package com.example.weightloss.ai.dto;

import lombok.Data;

@Data
public class FoodAnalysisRequest {
    private String foodDescription;  // 食物描述
    private String mealType;         // 餐次类型
    private String userId;           // 用户ID
}

@Data
public class FoodAnalysisResponse {
    private String foodName;         // 食物名称
    private Integer calories;        // 热量估算
    private Double protein;          // 蛋白质(g)
    private Double fat;              // 脂肪(g)
    private Double carbohydrate;     // 碳水化合物(g)
    private String nutritionalAdvice; // 营养建议
    private String recommendation;    // 饮食建议
    private Boolean analysisSuccess;  // 分析是否成功
    private String errorMessage;      // 错误信息
}