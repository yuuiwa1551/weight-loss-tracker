package com.example.weightloss.ai.service;

import com.example.weightloss.ai.dto.FoodAnalysisRequest;
import com.example.weightloss.ai.dto.FoodAnalysisResponse;

/**
 * AI食物分析服务接口
 * 可以对接不同的AI服务提供商
 */
public interface AIFoodAnalysisService {
    
    /**
     * 分析食物并返回营养信息
     */
    FoodAnalysisResponse analyzeFood(FoodAnalysisRequest request);
    
    /**
     * 批量分析食物
     */
    default FoodAnalysisResponse batchAnalyzeFood(Iterable<FoodAnalysisRequest> requests) {
        // 默认实现：逐个分析
        FoodAnalysisResponse response = new FoodAnalysisResponse();
        response.setAnalysisSuccess(false);
        response.setErrorMessage("批量分析未实现");
        return response;
    }
}