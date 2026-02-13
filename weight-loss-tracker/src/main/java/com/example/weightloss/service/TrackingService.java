package com.example.weightloss.service;

import com.example.weightloss.dto.CalorieTrackingDTO;
import com.example.weightloss.entity.DailySummary;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.ExerciseRecord;

import java.time.LocalDate;
import java.util.List;

public interface TrackingService {
    
    /**
     * 记录食物摄入
     */
    FoodRecord recordFood(FoodRecord foodRecord);
    
    /**
     * 记录运动消耗
     */
    ExerciseRecord recordExercise(ExerciseRecord exerciseRecord);
    
    /**
     * 获取某日热量追踪数据
     */
    CalorieTrackingDTO getDailyTracking(Long userId, LocalDate date);
    
    /**
     * 更新某日汇总数据
     */
    DailySummary updateDailySummary(Long userId, LocalDate date);
    
    /**
     * 获取用户近期追踪数据
     */
    List<CalorieTrackingDTO> getRecentTracking(Long userId, int days);
}