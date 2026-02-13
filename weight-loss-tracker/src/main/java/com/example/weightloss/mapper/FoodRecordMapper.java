package com.example.weightloss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weightloss.entity.FoodRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FoodRecordMapper extends BaseMapper<FoodRecord> {
    
    /**
     * 查询用户某日的所有饮食记录
     */
    @Select("SELECT * FROM food_records WHERE user_id = #{userId} AND record_date = #{recordDate} AND deleted = 0 ORDER BY create_time")
    List<FoodRecord> findByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
    
    /**
     * 查询用户某日期范围的饮食记录
     */
    @Select("SELECT * FROM food_records WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} AND deleted = 0 ORDER BY record_date DESC, create_time DESC")
    IPage<FoodRecord> findByUserIdAndDateRange(Page<FoodRecord> page, @Param("userId") Long userId, 
                                              @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 统计用户某日的总热量摄入
     */
    @Select("SELECT COALESCE(SUM(calories), 0) FROM food_records WHERE user_id = #{userId} AND record_date = #{recordDate} AND deleted = 0")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
}