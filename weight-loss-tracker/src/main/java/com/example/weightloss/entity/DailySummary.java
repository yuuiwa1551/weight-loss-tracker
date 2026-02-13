package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_summaries")
public class DailySummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private LocalDate recordDate;
    
    // 当日汇总数据
    private Integer totalCaloriesConsumed; // 总摄入热量
    private Integer totalCaloriesBurned; // 总消耗热量
    private Integer netCalories; // 净热量(摄入-消耗)
    
    // 营养汇总
    private Double totalProtein;
    private Double totalFat;
    private Double totalCarbohydrate;
    
    // 目标对比
    private Integer calorieGoal; // 当日热量目标
    private Integer calorieDifference; // 与目标的差值
    
    // 完成状态
    private String goalStatus; // UNDER/OVER/MEET
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}