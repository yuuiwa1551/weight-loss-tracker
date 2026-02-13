package com.example.weightloss.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_sport_record")
public class DailySportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate recordDate;    // 记录日期
    
    private String sportType;        // 运动类型
    private Integer stepCount;       // 步数
    private BigDecimal calorieConsumePer1000step; // 每千步消耗热量
    private BigDecimal singleConsume; // 单次消耗(计算字段)
    private String remark;           // 备注
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 逻辑删除字段（如果表中有）
    @TableLogic
    private Integer deleted;
}