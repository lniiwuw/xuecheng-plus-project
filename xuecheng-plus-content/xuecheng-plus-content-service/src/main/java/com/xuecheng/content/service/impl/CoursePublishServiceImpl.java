package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.ICourseBaseInfoService;
import com.xuecheng.content.service.ICoursePublishService;
import com.xuecheng.content.service.ITeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/12 19:24
 * @Description 课程预览接口实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoursePublishServiceImpl implements ICoursePublishService {

    private final ICourseBaseInfoService courseBaseInfoService;
    private final ITeachplanService teachplanService;
    private final CourseBaseMapper courseBaseMapper;
    private final CourseMarketMapper courseMarketMapper;
    private final CoursePublishPreMapper coursePublishPreMapper;
    private final CoursePublishMapper coursePublishMapper;
    private final MqMessageService mqMessageService;
    private final MediaServiceClient mediaServiceClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 查询课程基本信息和课程计划信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.queryCourseBaseById(courseId);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachplanTree);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        // 查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 查询课程基本信息、课程营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.queryCourseBaseById(courseId);
        // 查询课程计划
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        // 1. 约束
        String auditStatus = courseBaseInfoDto.getAuditStatus();
        // 1.1 上一次审核完后，方可提交审核
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("该课程现在属于待审核状态，审核完成后可再次提交");
        }
        // 1.2 本机构只允许提交本机构的课程
        if (!companyId.equals(courseBaseInfoDto.getCompanyId())) {
            XueChengPlusException.cast("本机构只允许提交本机构的课程");
        }
        // 1.3 没有上传图片，不允许提交
        if (StringUtils.isEmpty(courseBaseInfoDto.getPic())) {
            XueChengPlusException.cast("没有上传课程封面，不允许提交审核");
        }
        // 1.4 没有添加课程计划，不允许提交审核
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("没有添加课程计划，不允许提交审核");
        }

        // 2. 准备封装返回对象
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        // 营销信息和课程计划信息转json
        coursePublishPre.setMarket(JSON.toJSONString(courseMarket));
        coursePublishPre.setTeachplan(JSON.toJSONString(teachplanTree));
        coursePublishPre.setCompanyId(companyId);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 3. 设置预发布记录状态为已提交
        coursePublishPre.setStatus("202003");
        // 判断是否已经存在预发布记录，若存在，则更新
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            // 此时审核表可能还存在相应的课程已审核信息
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        // 4. 设置课程基本信息审核状态为已提交
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if(!"202004".equals(auditStatus)){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);
        //保存消息表
        saveCoursePublishMessage(courseId);
        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File file = null;
        try {
            // 配置 freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 加载模板
            // 选指定模板路径,classpath 下 templates 下
            // 得到 classpath 路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 设置字符编码
            configuration.setDefaultEncoding("utf-8");
            // 指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");
            // 准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            // 静态化
            // 参数 1：模板，参数 2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            // 将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content, StandardCharsets.UTF_8);
            // 上传临时文件
            file = File.createTempFile("course", ".html");
            // 输出流
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("生成课程静态化界面出错, 课程id：{}, 错误信息：{}", courseId, e.getMessage());
        }
        return file;
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course", courseId + ".html");
        if (course == null) {
            log.debug("远程调用媒资服务上传文件失败");
            XueChengPlusException.cast("远程调用媒资服务上传文件失败");
        }
    }

    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        // 查询缓存
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        if (jsonObj != null) {
            String jsonString = jsonObj.toString();
            log.debug("=================从缓存查=================, courseId：{}", courseId);
            return JSON.parseObject(jsonString, CoursePublish.class);
        } else {
            // 1. 分布式锁解决缓存击穿
            // 1.1 set nx 命令方式
            // Boolean ok = redisTemplate.opsForValue().setIfAbsent("coursequerylock" + courseId, "01", 30, TimeUnit.SECONDS);

            // 1.2 redisson分布式锁
            //每门课程设置一个锁
            RLock lock = redissonClient.getLock("coursequerylock:" + courseId);
            //获取锁
            lock.lock();
            try {
                String jsonString = (String) redisTemplate.opsForValue().get("course:" + courseId);
                if (StringUtils.isNotEmpty(jsonString)) {
                    return JSON.parseObject(jsonString, CoursePublish.class);
                }

                // Thread.sleep(60000);
                // 从数据库查询
                log.debug("从数据库查询..., courseId:{}", courseId);
                CoursePublish coursePublish = getCoursePublish(courseId);
                // 将空值也缓存,解决缓存穿透
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 30, TimeUnit.SECONDS);
                return coursePublish;
            } finally {
                // 释放锁
                lock.unlock();
            }
        }
    }

    /**
     * 保存课程发布信息
     *
     * @param courseId 课程id
     */
    private void saveCoursePublish(Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        // 设置发布状态为已发布
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        // 有则更新，无则新增
        if (coursePublishUpdate == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        // 更新课程基本信息表的发布状态为已发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 保存课程信息到消息表
     *
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }
}
