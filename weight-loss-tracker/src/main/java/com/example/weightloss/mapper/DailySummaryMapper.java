package com.example.weightloss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weightloss.entity.DailySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface DailySummaryMapper extends BaseMapper<DailySummary> {
    
    /**
     * 查询用户某日的汇总记录
     */
    @Select("SELECT * FROM daily_summaries WHERE user_id = #{userId} AND record_date = #{recordDate} AND deleted = 0")
    DailySummary findByUserIdAndDate(@Param("userId") Long userId, @Param("recordDate") LocalDate recordDate);
    
    /**
     * 查询用户最新的汇总记录
     */
    @Select("SELECT * FROM daily_summaries WHERE user_id = #{userId} AND deleted = 0 ORDER BY record_date DESC LIMIT 1")
    DailySummary findLatestByUserId(@Param("userId") Long userId);
}