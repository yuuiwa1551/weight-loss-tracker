package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_summary")
public class DailySummaryNew {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate recordDate;    // 记录日期
    
    private BigDecimal dailyTotalCalorie;  // 每日总热量摄入
    private BigDecimal dailySportConsume;  // 每日运动消耗
    private BigDecimal dailyCalorieLimit;  // 每日热量限制
    private BigDecimal dailyFixedConsume;  // 每日固定消耗
    private BigDecimal dailyCalorieGap;    // 热量差值(计算字段)
    private String gapStatus;              // 差值状态
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 逻辑删除字段（如果表中有）
    @TableLogic
    private Integer deleted;
}