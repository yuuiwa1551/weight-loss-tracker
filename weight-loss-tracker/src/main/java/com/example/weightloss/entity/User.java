package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    private String email;
    private String password; // 实际项目中应该加密存储
    
    // 用户基本信息
    private Integer age;
    private String gender; // MALE/FEMALE
    private Double height; // 身高(cm)
    private Double weight; // 当前体重(kg)
    private Double targetWeight; // 目标体重(kg)
    
    // 每日热量目标
    private Integer dailyCalorieGoal; // 每日热量摄入目标
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}