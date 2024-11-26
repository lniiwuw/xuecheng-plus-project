package com.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lniiwuw
 */
@Mapper
@Component
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {
    /**
     * 根据分片参数获取待处理任务
     * @param shardTotal    分片总数
     * @param shardIndex    分片序号
     * @param count         任务数
     * @return
     */
    @Select("SELECT * FROM media_process WHERE id % #{shardTotal} = #{shardIndex} AND (status = '1' or status = '3') AND fail_count < 3 LIMIT #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    @Update("UPDATE media_process SET status = '4' WHERE (status = '1' or status = '3') AND fail_count < 3 AND id = #{id}")
    int startTask(@Param("id") Long id);
}
