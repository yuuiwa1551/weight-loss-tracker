package com.example.weightloss.ai.service.impl;

import com.example.weightloss.ai.dto.FoodAnalysisRequest;
import com.example.weightloss.ai.dto.FoodAnalysisResponse;
import com.example.weightloss.ai.service.AIFoodAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAI食物分析服务实现
 * 需要配置OpenAI API Key
 */
@Slf4j
@Service
public class OpenAIFoodAnalysisServiceImpl implements AIFoodAnalysisService {
    
    @Value("${openai.api.key:}")
    private String apiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public FoodAnalysisResponse analyzeFood(FoodAnalysisRequest request) {
        FoodAnalysisResponse response = new FoodAnalysisResponse();
        
        try {
            // 构造AI提示词
            String prompt = buildPrompt(request);
            
            // 调用OpenAI API
            Map<String, Object> requestBody = buildRequestBody(prompt);
            // 这里需要实际调用API，暂时返回模拟数据
            
            // 模拟响应数据
            response = createMockResponse(request);
            response.setAnalysisSuccess(true);
            
            log.info("AI分析成功: {} -> {}卡路里", request.getFoodDescription(), response.getCalories());
            
        } catch (Exception e) {
            log.error("AI分析失败", e);
            response.setAnalysisSuccess(false);
            response.setErrorMessage("AI分析服务暂时不可用: " + e.getMessage());
        }
        
        return response;
    }
    
    private String buildPrompt(FoodAnalysisRequest request) {
        return String.format("""
            请分析以下食物的营养成分：
            食物描述：%s
            餐次类型：%s
            
            请提供以下信息：
            1. 食物标准名称
            2. 估算热量（卡路里）
            3. 蛋白质含量（克）
            4. 脂肪含量（克）
            5. 碳水化合物含量（克）
            6. 营养建议
            7. 饮食建议
            
            请以JSON格式回复，包含以上所有字段。
            """, request.getFoodDescription(), request.getMealType());
    }
    
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", new Object[]{
            Map.of("role", "user", "content", prompt)
        });
        body.put("temperature", 0.3);
        return body;
    }
    
    private FoodAnalysisResponse createMockResponse(FoodAnalysisRequest request) {
        FoodAnalysisResponse response = new FoodAnalysisResponse();
        response.setFoodName("模拟分析结果");
        response.setCalories(300);
        response.setProtein(15.0);
        response.setFat(10.0);
        response.setCarbohydrate(35.0);
        response.setNutritionalAdvice("营养均衡，建议搭配蔬菜");
        response.setRecommendation("适量食用，注意控制份量");
        return response;
    }
}