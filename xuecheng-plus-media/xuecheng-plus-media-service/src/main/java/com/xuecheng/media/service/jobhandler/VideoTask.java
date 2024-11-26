package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.IMediaFileProcessService;
import com.xuecheng.media.service.IMediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/9 19:55
 * @Description xxl-job任务处理类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoTask {

    private final IMediaFileProcessService mediaFileProcessService;
    private final IMediaFileService mediaFileService;

    // nacos配置中的ffmpeg路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        // 确定cpu的核心数
        int processors = Runtime.getRuntime().availableProcessors();
        // 查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        if (mediaProcessList == null || mediaProcessList.isEmpty()) {
            log.debug("查询到的待处理任务数量为0");
            return;
        }
        // 实际任务数量
        int size = mediaProcessList.size();
        // 创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {
                // 创建用于保存avi视频的文件
                File file = null;
                // 创建临时mp4文件
                File mp4File = null;
                try {
                    // 任务id
                    Long taskId = mediaProcess.getId();
                    // 开启任务
                    boolean getLock = mediaFileProcessService.startTask(taskId);
                    if (!getLock) {
                        log.debug("抢占任务失败，taskId：{}", taskId);
                        return;
                    }
                    // 桶、minio的avi视频访问路径、文件名
                    String bucket = mediaProcess.getBucket();
                    String filePath = mediaProcess.getFilePath();
                    // 文件md5
                    String fileId = mediaProcess.getFileId();
                    // 下载avi视频到本地文件
                    file = mediaFileService.downloadFileFromMinio(bucket, filePath);
                    if (file == null) {
                        // 保存失败信息
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }

                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    try {
                        // 文件名：prefix + 随机数 +suffix 构成的文件名
                        mp4File = File.createTempFile("mp4", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时MP4文件异常，{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件MP4异常");
                        return;
                    }
                    //转换后mp4文件的路径
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象，将avi视频转为MP4
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4File.getName(), mp4_path);
                    //开始视频转换，成功将返回success，失败返回失败原因
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        log.error("视频转码失败，bucket：{}，filepath:{}", bucket, filePath);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }

                    // 上传到minio的路径
                    String objectName = mediaFileService.getFilePathByMdt5(fileId, ".mp4");
                    // 将本地转码的mp4文件  上传到minio
                    try {
                        mediaFileService.addMediaFileToMinio(mp4_path, bucket, objectName);
                    } catch (Exception e) {
                        log.error("上 传文件出错，bucket：{}， objectName：{}", bucket, objectName);
                        return;
                    }
                    String url = "/" + bucket + "/" + objectName;
                    // 记录任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, result);
                } finally {
                    log.debug("清理临时文件");
                    if (file != null) {
                        file.delete();
                    }
                    if (mp4File != null) {
                        mp4File.delete();
                    }
                    // 计数器减一
                    countDownLatch.countDown();
                }
            });
        });
        // 等待，为了防止无线等待，这里设置一个超时时间为30分钟（很充裕了），若到时间还未处理完，则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
}
