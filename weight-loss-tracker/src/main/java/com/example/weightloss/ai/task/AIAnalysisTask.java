package com.example.weightloss.ai.task;

import com.example.weightloss.ai.dto.FoodAnalysisRequest;
import com.example.weightloss.ai.dto.FoodAnalysisResponse;
import com.example.weightloss.ai.service.AIFoodAnalysisService;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.mapper.FoodRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI分析定时任务
 * 定期处理未分析的食物记录
 */
@Slf4j
@Component
public class AIAnalysisTask {
    
    @Autowired
    private FoodRecordMapper foodRecordMapper;
    
    @Autowired
    private AIFoodAnalysisService aiFoodAnalysisService;
    
    /**
     * 每30分钟检查一次未AI分析的食物记录
     */
    @Scheduled(fixedDelay = 1800000) // 30分钟
    public void processPendingAIAnalysis() {
        try {
            // 查询未AI处理的食物记录
            List<FoodRecord> pendingRecords = foodRecordMapper.selectList(
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper.lambdaQuery(FoodRecord.class)
                    .eq(FoodRecord::getAiProcessed, false)
                    .orderByAsc(FoodRecord::getCreateTime)
                    .last("LIMIT 10") // 每次最多处理10条
            );
            
            if (pendingRecords.isEmpty()) {
                log.debug("没有待处理的AI分析任务");
                return;
            }
            
            log.info("开始处理{}条待AI分析的食物记录", pendingRecords.size());
            
            for (FoodRecord record : pendingRecords) {
                try {
                    // 构造AI分析请求
                    FoodAnalysisRequest request = new FoodAnalysisRequest();
                    request.setFoodDescription(record.getFoodDescription());
                    request.setMealType(record.getMealType());
                    
                    // 调用AI分析
                    FoodAnalysisResponse response = aiFoodAnalysisService.analyzeFood(request);
                    
                    if (response.getAnalysisSuccess()) {
                        // 更新记录
                        record.setFoodName(response.getFoodName());
                        record.setCalories(response.getCalories());
                        record.setProtein(response.getProtein());
                        record.setFat(response.getFat());
                        record.setCarbohydrate(response.getCarbohydrate());
                        record.setAiAnalysis(response.getNutritionalAdvice() + "\n" + response.getRecommendation());
                        record.setAiProcessed(true);
                        
                        foodRecordMapper.updateById(record);
                        log.info("AI分析完成: {} -> {}卡路里", record.getFoodDescription(), response.getCalories());
                    } else {
                        log.warn("AI分析失败: {} - {}", record.getFoodDescription(), response.getErrorMessage());
                    }
                    
                } catch (Exception e) {
                    log.error("处理单条记录失败: " + record.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("AI分析定时任务执行失败", e);
        }
    }
}