package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.ICourseMarketService;
import org.springframework.stereotype.Service;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/29 10:40
 * @Description 课程营销信息管理类
 */
@Service
public class CourseMarketServiceImpl extends ServiceImpl<CourseMarketMapper, CourseMarket> implements ICourseMarketService {
}
