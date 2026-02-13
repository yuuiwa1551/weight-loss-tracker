package com.example.weightloss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weightloss.entity.ExerciseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExerciseRecordMapper extends BaseMapper<ExerciseRecord> {
    
    /**
     * 查询用户某日的所有运动记录
     */
    @Select("SELECT * FROM exercise_records WHERE user_id = #{userId} AND record_date = #{recordDate} AND deleted = 0 ORDER BY create_time")
    List<ExerciseRecord> findByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
    
    /**
     * 查询用户某日期范围的运动记录
     */
    @Select("SELECT * FROM exercise_records WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY record_date DESC, create_time DESC")
    IPage<ExerciseRecord> findByUserIdAndDateRange(Page<ExerciseRecord> page, @Param("userId") Long userId, 
                                                  @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 统计用户某日的总热量消耗
     */
    @Select("SELECT COALESCE(SUM(calories_burned), 0) FROM exercise_records WHERE user_id = #{userId} AND record_date = #{recordDate} AND deleted = 0")
    Double sumCaloriesBurnedByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
}