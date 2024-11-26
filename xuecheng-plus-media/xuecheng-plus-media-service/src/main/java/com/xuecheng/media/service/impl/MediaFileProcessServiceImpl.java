package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.IMediaFileProcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/8 20:26
 * @Description IMediaFileProcessService接口实现类
 */
@Service
@RequiredArgsConstructor
public class MediaFileProcessServiceImpl implements IMediaFileProcessService {

    private final MediaProcessMapper mediaProcessMapper;
    private final MediaFilesMapper mediaFilesMapper;
    private final MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 查找更新任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return ;
        }

        //=====================更新失败====================================
        if (status.equals("3")) {
            // 更改字段信息
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            // 更新media_process表
            mediaProcessMapper.updateById(mediaProcess);
            return ;
        }

        //=====================更新成功====================================
        // 更新media_file的url信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 更新media_process表状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        // mediaProcessMapper.updateById(mediaProcess);

        // 将media_process表记录插入media_process_history表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 从media_process中删除成功记录
        mediaProcessMapper.deleteById(taskId);
    }

    @Override
    public boolean startTask(Long taskId) {
        int result = mediaProcessMapper.startTask(taskId);
        return result > 0;
    }

}
