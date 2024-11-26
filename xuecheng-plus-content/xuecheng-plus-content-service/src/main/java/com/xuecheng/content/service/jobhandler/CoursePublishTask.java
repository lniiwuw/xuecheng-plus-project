package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.po.CourseIndex;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.ICoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/14 18:43
 * @Description 定时调度课程发布任务
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    private final ICoursePublishService coursePublishService;
    private final SearchServiceClient searchServiceClient;
    private final CoursePublishMapper coursePublishMapper;

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        // 调用sdk中MessageProcessAbstract的执行方法
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        // 从mqMessage拿到课程id
        long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        // 静态化页面写入minio
        generateCourseHtml(mqMessage, courseId);

        // 向elasticsearch写索引数据
        saveCourseIndex(mqMessage, courseId);

        // 向redis写缓存数据
        // saveCourseCache(mqMessage, courseId);
        return true;
    }

    /**
     * 生成课程静态化页面并上传至文件系统
     *
     * @param mqMessage 课程发布相关消息
     * @param courseId 课程id
     */
    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始进行课程静态化,课程 id:{}", courseId);
        //消息 id
        Long id = mqMessage.getId();
        //消息处理的 service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("课程静态化已处理直接返回，课程id:{}", courseId);
            return;
        }

        // 生成静态化页面文件
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("课程静态化异常");
        }
        // 上传文件到minio
        coursePublishService.uploadCourseHtml(courseId, file);
        //将任务状态修改为完成
        mqMessageService.completedStageOne(id);
    }

    /**
     * 保存课程索引信息
     *
     * @param mqMessage 课程发布相关消息
     * @param courseId 课程id
     */
    public void saveCourseIndex(MqMessage mqMessage, long courseId){
        log.debug("保存课程索引信息,课程 id:{}",courseId);
        //消息 id
        Long id = mqMessage.getId();
        //消息处理的 service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(id);
        if (stageTwo > 0) {
            log.debug("课程索引信息已处理直接返回，课程id:{}", courseId);
            return ;
        }

        // 查询课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        // 拷贝信息
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);

        // 保存索引信息
        Boolean add = searchServiceClient.add(courseIndex);
        if (!add) {
            log.debug("远程调用添加索引服务失败");
            XueChengPlusException.cast("远程调用添加索引服务失败");
        }
        //将任务状态修改为完成
        mqMessageService.completedStageTwo(id);
    }

    /**
     * 将课程信息缓存至 redis
     *
     * @param mqMessage 课程发布相关消息
     * @param courseId 课程id
     */
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        log.debug("将课程信息缓存至 redis,课程 id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
