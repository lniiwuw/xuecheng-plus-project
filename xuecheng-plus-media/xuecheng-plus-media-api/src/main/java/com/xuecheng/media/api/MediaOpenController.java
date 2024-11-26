package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.IMediaFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 20:52
 * @Description 课程预览界面的媒资信息对应控制类
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/open")
public class MediaOpenController {

    private final IMediaFileService mediaFileService;

    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getMediaUrl(@PathVariable String mediaId) {
        MediaFiles mediaFile = mediaFileService.getFileById(mediaId);
        if (mediaFile == null || StringUtils.isEmpty(mediaFile.getUrl())) {
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return RestResponse.success(mediaFile.getUrl());
    }
}