package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_calorie_record")
public class DailyCalorieRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate recordDate;    // 记录日期
    
    private String mealTime;         // 用餐时间
    private Integer mealType;        // 餐次类型(数字)
    private String foodName;         // 食物名称
    private String foodWeight;       // 食物重量
    private BigDecimal singleCalorie; // 单份热量
    private String remark;           // 备注
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 逻辑删除字段，用于标记记录是否被删除（0:未删除，1:已删除）
    @TableLogic
    private Integer deleted;
}