package com.example.weightloss.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weightloss.entity.DailyCalorieRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyCalorieRecordMapper extends BaseMapper<DailyCalorieRecord> {
    // 可以在这里添加自定义查询方法
}