package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.IMediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/2 19:15
 * @Description 媒资文件管理控制类
 */
@Slf4j
@Api(value = "媒资文件管理接口", tags = "媒资文件管理接口")
@RestController
@RequiredArgsConstructor
public class MediaFilesController {

    private final IMediaFileService mediaFileService;

    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto) {
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiles(companyId, pageParams, queryMediaParamsDto);
    }

    // consumes属性用于指定控制器可以处理的请求内容类型
    @ApiOperation(value = "上传文件接口", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile upload,
                                      @RequestParam(value = "folder", required = false) String folder,
                                      @RequestParam(value = "objectName", required = false) String objectName) {
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        // 设置文件名为包含盘符的路径信息, getName方法为?
        uploadFileParamsDto.setFilename(upload.getOriginalFilename());
        log.info("{}", upload.getName());
        String contentType = upload.getContentType();
        uploadFileParamsDto.setFileType(contentType);
        // 设置图片类型或者其他类型
        assert contentType != null;
        if (contentType.contains("image")) {
            uploadFileParamsDto.setFileType("001001");
        } else {
            uploadFileParamsDto.setFileType("001003");
        }
        uploadFileParamsDto.setFileSize(upload.getSize());
        long companyId = 1232141425L;
        try {
            return mediaFileService.uploadFile(companyId, uploadFileParamsDto, upload.getBytes(), folder, objectName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new XueChengPlusException("文件上传过程中出错");
        }
    }

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        return RestResponse.success(mediaFiles.getUrl());
    }
}
