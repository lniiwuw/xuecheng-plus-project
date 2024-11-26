package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.IMediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/5 21:18
 * @Description 大文件（视频）上传控制类
 */
@Api(value = "大文件上传接口", tags = "大文件（视频）上传接口")
@RestController
@RequiredArgsConstructor
public class BigFilesController {

    private final IMediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前检查分块")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") @ApiParam("整个文件的md5值") String fileMd5,
                                            @RequestParam("chunk") @ApiParam("分块文件序号") int chunk) {
        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") @ApiParam("分块文件") MultipartFile file,
                                    @RequestParam("fileMd5") @ApiParam("整个文件的md5") String fileMd5,
                                    @RequestParam("chunk") @ApiParam("分块文件序号") int chunk) throws IOException {
        return mediaFileService.uploadChunk(fileMd5, chunk, file.getBytes());
    }

    @ApiOperation(value = "合并分块文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("chunkTotal") int chunkTotal) {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setRemark("");
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("课程视频");
        return mediaFileService.mergeChunk(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
