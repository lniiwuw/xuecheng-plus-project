package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.ITeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/10/30 20:55
 * @Description 课程计划管理类
 */
@Api(tags = "课程计划编辑接口")
@RestController
@RequiredArgsConstructor
public class TeachPlanController {

    private final ITeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNode(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    @ApiOperation("新增或修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId) {
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("向上/向下移动课程计划")
    @PostMapping("/teachplan/{moveType}/{teachplanId}")
    public void moveTeachplan(@PathVariable String moveType, @PathVariable Long teachplanId) {
        teachplanService.moveTeachplan(moveType, teachplanId);
    }

    @ApiOperation("课程计划与媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto) {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

    @ApiOperation("向上/向下移动课程计划")
    @GetMapping("/teachplan/{teachplanId}")
    public Teachplan getTeachplan(@PathVariable Long teachplanId) {
        return teachplanService.getTeachPlan(teachplanId);
    }
}
