package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("exercise_records")
public class ExerciseRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private LocalDate recordDate;
    
    // 运动信息
    private String exerciseType; // 运动类型
    private String exerciseName; // 具体运动名称
    
    // 运动数据
    private Integer duration; // 持续时间(分钟)
    private Double caloriesBurned; // 消耗热量
    
    // 运动强度
    private String intensity; // LOW/MEDIUM/HIGH
    
    // 备注
    private String notes;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}