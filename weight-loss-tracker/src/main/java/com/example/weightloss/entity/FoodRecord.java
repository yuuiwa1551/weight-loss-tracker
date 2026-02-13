package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("food_records")
public class FoodRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private LocalDate recordDate;
    
    // 食物信息
    private String foodName;
    private String foodDescription; // 食物详细描述
    
    // 热量和营养信息
    private Integer calories; // 热量(卡路里)
    private Double protein; // 蛋白质(g)
    private Double fat; // 脂肪(g)
    private Double carbohydrate; // 碳水化合物(g)
    
    // 餐次分类
    private String mealType; // BREAKFAST/LUNCH/DINNER/SNACK
    
    // LLM分析结果
    private String aiAnalysis; // AI分析结果
    private Boolean aiProcessed; // 是否已AI处理
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}