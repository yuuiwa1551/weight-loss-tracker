package com.example.weightloss.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CalorieTrackingDTO {
    private Long userId;
    private LocalDate date;
    private Integer totalCaloriesConsumed;  // 总摄入热量
    private Integer totalCaloriesBurned;    // 总消耗热量
    private Integer netCalories;           // 净热量
    private Integer calorieGoal;           // 热量目标
    private Integer calorieDifference;     // 与目标差值
    private String goalStatus;             // 目标完成状态
    private Double totalProtein;           // 总蛋白质
    private Double totalFat;               // 总脂肪
    private Double totalCarbohydrate;      // 总碳水化合物
}