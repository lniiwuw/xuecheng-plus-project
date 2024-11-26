package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.ICourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/24 17:06
 * @Description 课程信息管理类
 */
@Api(tags = "课程信息管理接口")
@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseBaseInfoController {

    private final ICourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')") // 权限包含在jwt
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamDto queryCourseParamDto) {
        // 当前用户身份
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        assert user != null;
        Long companyId = user.getCompanyId();
        log.debug("当前用户的id：{}， companyId：{}", user.getId(), user.getCompanyId());
        // 调用 service 获取数据 （实现细粒度授权，本机构只能查询自己机构的课程列表）
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamDto);
    }

    @ApiOperation("课程添加接口")
    @PostMapping("")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation("根据课程id查询信息")
    @GetMapping("/{courseId}")
    public CourseBaseInfoDto getCourseById(@PathVariable Long courseId) {
        return courseBaseInfoService.queryCourseBaseById(courseId);
    }

    @ApiOperation("课程修改接口")
    @PutMapping("")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto) {
        long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        long companyId = 1232141425L;
        courseBaseInfoService.deleteCourse(companyId, courseId);
    }
}
