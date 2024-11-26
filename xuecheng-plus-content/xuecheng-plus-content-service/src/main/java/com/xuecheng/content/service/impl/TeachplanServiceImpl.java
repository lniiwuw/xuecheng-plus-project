package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.ITeachplanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/31 10:55
 * @Description 课程计划管理类
 */
@Service
@RequiredArgsConstructor
public class TeachplanServiceImpl implements ITeachplanService {

    private final TeachplanMapper teachplanMapper;
    private final TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 根据课程计划id是否为null判断是新增操作还是修改操作
        Long id = saveTeachplanDto.getId();
        if (id == null) {
            // 将新增信息拷贝到新创建的Teachplan类
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplan.setCreateDate(LocalDateTime.now());
            // 查找课程章节的排序字段，orderby字段决定显示章节顺序，见TeachplanMapper#selectTreeNodes
            int order = getTeachplanOrderbyMax(teachplan.getCourseId(), teachplan.getParentid()) + 1;
            teachplan.setOrderby(order);
            int insert = teachplanMapper.insert(teachplan);
            if (insert <= 0) {
                XueChengPlusException.cast("新增章节失败！");
            }
        } else {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            int update = teachplanMapper.updateById(teachplan);
            if (update <= 0) {
                XueChengPlusException.cast("更新章节失败！");
            }
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long teachplanId) {
        if (teachplanId == null) {
            XueChengPlusException.cast("章节计划id为空！");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 判断当前课程计划是章还是节
        Integer grade = teachplan.getGrade();
        if (grade == 1) {
            // 当前课程是大章节
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>()
                    .eq(Teachplan::getParentid, teachplanId);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            // 删除课程计划信息
            teachplanMapper.deleteById(teachplanId);
        } else {
            // 当前课程是小章节
            teachplanMapper.deleteById(teachplanId);
            // TODO: 删除媒资信息
        }
    }

    @Transactional
    @Override
    public void moveTeachplan(String moveType, Long teachplanId) {
        // 当前的teachplan
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 排序字段
        Integer orderby = teachplan.getOrderby();
        // 层级
        Integer grade = teachplan.getGrade();
        // 小节移动是比较同一章节id下的orderby
        Long parentid = teachplan.getParentid();
        // 章节移动是比较同一课程id下的orderby
        Long courseId = teachplan.getCourseId();
        // 根据不同情况更新queryWrapper
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        if ("moveup".equals(moveType)) {
            if (grade == 1) {
                // 找到相同courseId，orderby更小的teachplan
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("limit 1");
            } else {
                // 找到相同parentId，orderby更小的teachplan
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("limit 1");
            }
        } else {
            if (grade == 1) {
                // 找到相同courseId，orderby更大的teachplan
                queryWrapper.eq(Teachplan::getCourseId, courseId)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("limit 1");
            } else {
                // 找到相同parentId，orderby更大的teachplan
                queryWrapper.eq(Teachplan::getParentid, parentid)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("limit 1");
            }
        }
        Teachplan swapTeachplan = teachplanMapper.selectOne(queryWrapper);
        exchangeTeachplan(teachplan, swapTeachplan);
    }

    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        // 获取教学计划 id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        // 查询教学计划
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengPlusException.cast(String.format("教学计划 %s 不存在", teachplanId));
        }
        // 得到层级
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            XueChengPlusException.cast("只允许第二级教学计划绑定媒资文件");
        }

        // 删除原有教学计划的绑定关系
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachplanId));

        // 设置相关信息
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    @Override
    public Teachplan getTeachPlan(Long teachPlanId) {
        return teachplanMapper.selectById(teachPlanId);
    }

    /**
     * 交换数据库表中两个Teachplan的orderby字段
     * @param currentTeachplan
     * @param swapTeachplan
     */
    private void exchangeTeachplan(Teachplan currentTeachplan, Teachplan swapTeachplan) {
        if (swapTeachplan == null) {
            // 已经位于最顶端或者最低端
            XueChengPlusException.cast("无法继续移动！");
        }
        Integer orderby = currentTeachplan.getOrderby();
        currentTeachplan.setOrderby(swapTeachplan.getOrderby());
        swapTeachplan.setOrderby(orderby);
        int update1 = teachplanMapper.updateById(currentTeachplan);
        if (update1 <= 0) {
            XueChengPlusException.cast("移动失败");
        }

        int update2 = teachplanMapper.updateById(swapTeachplan);
        if (update2 <= 0) {
            XueChengPlusException.cast("移动失败");
        }
    }

    /**
     * 查询courseId相同且parentId相同的数据的orderby字段最大值
     * @param courseId
     * @param parentId
     * @return int
     */
    private int getTeachplanOrderbyMax(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                    .eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectOrderbyMax(queryWrapper);
    }
}
