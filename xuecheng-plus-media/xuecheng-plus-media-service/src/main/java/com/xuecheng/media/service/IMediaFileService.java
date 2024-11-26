package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

import java.io.File;

/**
 * <p>
 * 媒资信息 服务类
 * </p>
 *
 * @author lniiwuw
 * @since 2024-11-02
 */
public interface IMediaFileService {

    /**
     * 媒资文件查询方法
     *
     * @param companyId           机构id
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return PageResult<MediaFiles>
     */
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 上传文件的通用接口
     *
     * @param companyId           机构id
     * @param uploadFileParamsDto 文件信息
     * @param bytes               文件字节数组
     * @param folder              桶下边的子目录
     * @param objectName          对象名称
     * @return com.xuecheng.media.model.dto.UploadFileResultDto
     */
    UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName);

    /**
     * 上传媒资文件到minio
     *
     * @param bytes      文件的字节数组
     * @param bucket     minio的桶
     * @param objectName minio存储的对象名
     */
    void addMediaFileToMinio(byte[] bytes, String bucket, String objectName);

    /**
     * 上传本地媒资文件到minio
     *
     * @param filePath   本地文件路径
     * @param bucket     minio的桶
     * @param objectName minio存储的对象名
     */
    void addMediaFileToMinio(String filePath, String bucket, String objectName);

    /**
     * 上传媒资信息到数据库
     *
     * @param companyId 机构id
     * @param uploadFileParamsDto 文件上传请求参数类
     * @param bucket 对应minio中的bucket
     * @param objectName 对应bucket中的文件路径名称
     * @param fileId md5
     * @return MediaFiles
     */
    MediaFiles addMediaFileToDb(long companyId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName, String fileId);

    /**
     * 上传分块到minio
     *
     * @param fileMd5 文件md5
     * @param chunkIndex 分块序号
     * @param bytes 分块的数据
     * @return RestResponse
     */
    RestResponse uploadChunk(String fileMd5, int chunkIndex, byte[] bytes);

    /**
     * 合并文件分块
     *
     * @param companyId 机构id
     * @param fileMd5 文件md5
     * @param chunkTotal 文件分块数量
     * @param uploadFileParamsDto 文件上传请求参数类
     * @return RestResponse
     */
    RestResponse mergeChunk(long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    /**
     * 根据md5查询数据库和minio中是否存在相应的文件
     *
     * @param fileMd5 文件的md5值
     * @return RestResponse<Boolean>
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 判断minio相应目录下是否存在对应的分块文件
     *
     * @param fileMd5 文件的md5
     * @param chunkIndex 分块文件的序号
     * @return RestResponse<Boolean>
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 从minio下载指定文件并返回
     *
     * @param bucket 桶
     * @param objectName 文件路径
     * @return File
     */
    File downloadFileFromMinio(String bucket, String objectName);

    /**
     * 根据md5和文件扩展名得到合并文件路径
     *
     * @param fileMd5 文件md5
     * @param extName 文件扩展名
     * @return String
     */
    String getFilePathByMdt5(String fileMd5, String extName);

    /**
     * 根据文件id查询文件信息
     *
     * @param fileId 文件id
     * @return String
     */
    MediaFiles getFileById(String fileId);
}
