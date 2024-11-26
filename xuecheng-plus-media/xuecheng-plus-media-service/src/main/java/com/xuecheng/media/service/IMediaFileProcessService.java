package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import io.minio.GetObjectArgs;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/8 20:26
 * @Description 待处理媒资文件相关接口
 */
public interface IMediaFileProcessService {
    /**
     * 获取待处理任务
     *
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return 待处理任务集合
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     * 保存任务结果
     *
     * @param taskId   任务id
     * @param status   任务状态
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     */
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

    /**
     * 开启任务，实现方式：update对应media_process表，update成功表示获取到锁
     *
     * @param taskId 任务id
     * @return boolean  true：当前线程获取任务    false：当前线程没有获取到对应任务
     */
    boolean startTask(Long taskId);
}
