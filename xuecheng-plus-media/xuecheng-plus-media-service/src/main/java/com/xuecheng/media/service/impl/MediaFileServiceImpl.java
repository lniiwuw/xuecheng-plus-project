package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.IMediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/2 19:43
 * @Description 媒资文件管理业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileServiceImpl implements IMediaFileService {

    private final MediaFilesMapper mediaFilesMapper;
    private final MinioClient minioClient;
    private final MediaProcessMapper mediaProcessMapper;

    // 当前类的代理对象，用于transactional事务控制
    @Lazy
    @Autowired
    IMediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucketVideoFiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

    }


    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        // 文件的mdt值
        String fileMd5 = DigestUtils.md5DigestAsHex(bytes);
        // 得到存放的文件夹
        if (folder == null) {
            folder = getFolder(true, true, true);
        } else if (folder.charAt(folder.length() - 1) != '/') {
            folder += '/';
        }
        if (StringUtils.isEmpty(objectName)) {
            // 拿到文件名
            String filename = uploadFileParamsDto.getFilename();
            // 设置文件名= 文件的md5码 + 文件后缀名（文件后缀名能否为空？）
            objectName = fileMd5 + filename.substring(filename.lastIndexOf('.'));
        }
        objectName = folder + objectName;
        try {
            // 上传文件到minio
            addMediaFileToMinio(bytes, bucketFiles, objectName);
            // 上传文件到数据库
            MediaFiles mediaFiles = currentProxy.addMediaFileToDb(companyId, uploadFileParamsDto, bucketFiles, objectName, fileMd5);
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        } catch (Exception e) {
            e.printStackTrace();
            log.debug("文件上传失败！");
        }
        return null;
    }

    @Override
    public void addMediaFileToMinio(byte[] bytes, String bucket, String objectName) {
        // 得到文件的mimeType
        String contentType = this.getMimeTypeByExtension(objectName);
        // 将字节数组转为ByteArrayInputStream流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            PutObjectArgs build = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1) // -1 表示文件分片按 5M(不小于 5M,不大于 5T),分片数量最大 10000
                    .contentType(contentType)
                    .build();
            minioClient.putObject(build);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错，bucket：{}， object：{}， 错误信息：{}", bucket, objectName, e.getMessage());
            XueChengPlusException.cast("上传文件到文件系统出错！");
        }
    }

    @Override
    public void addMediaFileToMinio(String filePath, String bucket, String objectName) {
        // 得到文件的mimeType
        String contentType = this.getMimeTypeByExtension(objectName);
        try {
            UploadObjectArgs build = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build();
            minioClient.uploadObject(build);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错，bucket：{}， object：{}， 错误信息：{}", bucket, objectName, e.getMessage());
            XueChengPlusException.cast("上传文件到文件系统出错！");
        }
    }

    // 不在uploadFile方法上加事务注解的原因：
    // 如果在uploadFile方法上添加@Transactional，当调用uploadFile方法前会开启数据库事务，如果上传文件过程时间较长（例如用户在上传超大视频文件），
    // 那么数据库的食物持续时间也会变长（因为在uploadFile方法中，我们即要将文件上传到minio，又要将文件信息写入数据库），这样数据库连接释放就慢，最终导致数据库链接不够用
    @Transactional
    @Override
    public MediaFiles addMediaFileToDb(long companyId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName, String fileId)  {
        // 查找media_file表中是否有MD5对应的文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        // 得到文件对应的mimetype
        String contentType = getMimeTypeByExtension(objectName);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            // 审核通过
            mediaFiles.setAuditStatus("002003");
            // 初始状态正常显示
            mediaFiles.setStatus("1");
            // 图片和mp4可以直接设置url
            if (contentType.contains("image") || contentType.contains("/mp4")) {
                mediaFiles.setUrl("/" + bucket + "/" + objectName);
            }
            // 主键冲突、sql语法错误、连接不可用情况下会抛出异常，即操作本身出错才会抛出异常；其他情况会返回一个整数值
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.error("上传文件到数据库失败，bucket：{}, objectName：{}, fileId：{}", bucket, objectName, fileId);
                return null;
            }

            // 对 avi 视频添加到待处理任务表
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        // 若数据库中不存在，则返回false 表示不存在
        if (mediaFiles == null) {
            return RestResponse.success(false);
        }

        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilename())
                    .build());
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 得到分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketVideoFiles)
                    .object(chunkFilePath)
                    .build());
            // 文件不存在，返回false
            if (inputStream == null) {
                return RestResponse.success(false);
            }
        } catch (Exception e) {
            return RestResponse.success(false);
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunkIndex, byte[] bytes) {
        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunkIndex;
        try {
            addMediaFileToMinio(bytes, bucketVideoFiles, chunkFilePath);
            log.debug("上传分块成功，fileMd5:{}, chuckIndex:{}", fileMd5, chunkIndex);
            return RestResponse.success(true);
        } catch (Exception e) {
            log.error("上传分块到minio出错，fileMd5:{}, chuckIndex:{}", fileMd5, chunkIndex);
        }
        return RestResponse.validfail("上传文件失败", false);
    }

    @Override
    public RestResponse mergeChunk(long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // ===================获取分块文件===================================================
        // 分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 将所有分块文件路径组成ComposeSource
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucketVideoFiles)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        // =================合并文件=========================================================
        // 文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 文件扩展名
        String extName = filename.substring(filename.lastIndexOf('.'));
        // 合并文件路径
        String mergeFilePath = getFilePathByMdt5(fileMd5, extName);
        try {
            minioClient.composeObject(ComposeObjectArgs.builder()
                    .sources(sourceObjectList)
                    .bucket(bucketVideoFiles)
                    .object(mergeFilePath)
                    .build());
            log.debug("合并文件成功：{}", mergeFilePath);
        } catch (Exception e) {
            log.error("合并文件失败, fileMd5: {}, 异常：{}", fileMd5, e.getMessage());
            return RestResponse.validfail("合并文件失败", false);
        }

        // =================校验md5=========================================================
        // 下载合并后的文件
        File minioFile = downloadFileFromMinio(bucketVideoFiles, mergeFilePath);
        if (minioFile == null) {
            return RestResponse.validfail("下载合并文件失败", false);
        }

        try(FileInputStream inputStream = new FileInputStream(minioFile)) {
            // minio上合并文件的md5
            String md5Hex = DigestUtils.md5DigestAsHex(inputStream);
            log.debug("minio合并文件的md5：{}", md5Hex);
            // 校验mdt，不一致说明文件不完整
            if (!md5Hex.equals(fileMd5)) {
                return RestResponse.validfail("文件合并校验失败，最终上传失败");
            }
            // 文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        } catch (Exception e) {
            log.error("文件校验失败，fileMd5:{}, 异常：{}", fileMd5, e.getMessage());
            return RestResponse.validfail("文件合并校验失败，最终上传失败");
        } finally {
            // 删除临时文件
            minioFile.delete();
        }
        // 文件入库
        currentProxy.addMediaFileToDb(companyId, uploadFileParamsDto, bucketVideoFiles, mergeFilePath, fileMd5);

        // =================清空分块文件=============================================
        clearChunkFiles(chunkFileFolderPath, bucketVideoFiles, chunkTotal);
        return RestResponse.success(true);
    }

    @Override
    public MediaFiles getFileById(String fileId) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            XueChengPlusException.cast("文件不存在");
        }
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)) {
            XueChengPlusException.cast("文件还没有转码处理，请稍后预览");
        }
        return mediaFiles;
    }

    /**
     * 添加待处理任务（添加avi视频转码任务）
     *
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 文件名
        String filename = mediaFiles.getFilename();
        // 文件的mimeType
        String mimeType = getMimeTypeByExtension(filename);
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            // 状态为未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            // 初始失败次数为0
            mediaProcess.setFailCount(0);
            int insert = mediaProcessMapper.insert(mediaProcess);
            if (insert <= 0) {
                log.error("添加待处理avi视频失败, fileMd5:{}, bucket：{}", mediaFiles.getFileId(), mediaFiles.getBucket());
                XueChengPlusException.cast("添加待处理avi视频失败");
            }
            log.debug("添加待处理avi视频成功, fileMd5:{}, bucket：{}", mediaFiles.getFileId(), mediaFiles.getBucket());
        }
    }

    /**
     * 清空分块文件
     *
     * @param chunkFileFolderPath 分块文件路径
     * @param bucket 桶
     * @param chunkTotal 分块数量
     */
    private void clearChunkFiles(String chunkFileFolderPath, String bucket, int chunkTotal) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucket)
                    .objects(deleteObjects)
                    .build());
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("");
        }
    }

    @Override
    public File downloadFileFromMinio(String bucket, String objectName) {
        File minioFile = null;
        FileOutputStream fileOutputStream = null;
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            minioFile = File.createTempFile("minio", ".merge");
            fileOutputStream = new FileOutputStream(minioFile);
            IOUtils.copy(inputStream, fileOutputStream);
            log.debug("下载文件成功，bucket:{}， objectName：{}", bucket, objectName);
            return minioFile;
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("下载文件失败，bucket:{}， objectName：{}", bucket, objectName);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 根据文件名得到对应的媒体类型
     *
     * @param objectName 文件名
     * @return 对应的媒体类型
     */
    private String getMimeTypeByExtension(String objectName) {
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认content-type为未知二进制流
        if (objectName.contains(".")) { // 判断对象名是否包含 .
            // 有 .  则划分出扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            // 根据扩展名得到content-type，如果为未知扩展名，例如 .abc之类的东西，则会返回null
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            // 如果得到了正常的content-type，则重新赋值，覆盖默认类型
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }

    /**
     * 根据当前时间自动生成目录
     *
     * @param year 年
     * @param month 月
     * @param day 日
     * @return String 目录
     */
    private String getFolder(boolean year, boolean month, boolean day) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = simpleDateFormat.format(date);
        String[] split = dateString.split("-");
        StringBuilder stringBuilder = new StringBuilder();
        if (year) {
            stringBuilder.append(split[0]).append("/");
        }
        if (month) {
            stringBuilder.append(split[1]).append("/");
        }
        if (day) {
            stringBuilder.append(split[2]).append("/");
        }
        return stringBuilder.toString();
    }

    /**
     * 查找md5文件对应的分块在minio中的存储目录
     *
     * @param fileMd5 文件的md5
     * @return String
     */
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/";
    }

    @Override
    public String getFilePathByMdt5(String fileMd5, String extName) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + extName;
    }

}
