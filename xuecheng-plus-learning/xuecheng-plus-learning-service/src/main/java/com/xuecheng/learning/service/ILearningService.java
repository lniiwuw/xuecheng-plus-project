package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;

/**
 * @author lniiwuw
 * @version v1.0.0
 * @Date 2024/11/22 18:44
 * @Description 获取视频服务接口
 */
public interface ILearningService {
    /**
     * 判断用户相应课程的学习资格，并获取视频连接
     *
     * @param userId 用户id
     * @param courseId 课程id
     * @param teachplanId 教学计划id
     * @param mediaId 媒资文件id
     * @return RestResponse<String>
     */
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
