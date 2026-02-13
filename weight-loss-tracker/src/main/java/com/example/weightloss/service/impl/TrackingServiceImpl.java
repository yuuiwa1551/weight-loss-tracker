package com.example.weightloss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.weightloss.dto.CalorieTrackingDTO;
import com.example.weightloss.entity.*;
import com.example.weightloss.mapper.*;
import com.example.weightloss.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TrackingServiceImpl implements TrackingService {
    
    @Autowired
    private FoodRecordMapper foodRecordMapper;
    
    @Autowired
    private ExerciseRecordMapper exerciseRecordMapper;
    
    @Autowired
    private DailySummaryMapper dailySummaryMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public FoodRecord recordFood(FoodRecord foodRecord) {
        // 设置默认值
        if (foodRecord.getAiProcessed() == null) {
            foodRecord.setAiProcessed(false);
        }
        
        foodRecordMapper.insert(foodRecord);
        log.info("记录食物摄入: 用户ID={}, 食物={}, 热量={}", 
                foodRecord.getUserId(), foodRecord.getFoodName(), foodRecord.getCalories());
        
        // 更新当日汇总
        updateDailySummary(foodRecord.getUserId(), foodRecord.getRecordDate());
        
        return foodRecord;
    }
    
    @Override
    @Transactional
    public ExerciseRecord recordExercise(ExerciseRecord exerciseRecord) {
        exerciseRecordMapper.insert(exerciseRecord);
        log.info("记录运动消耗: 用户ID={}, 运动={}, 消耗热量={}", 
                exerciseRecord.getUserId(), exerciseRecord.getExerciseName(), exerciseRecord.getCaloriesBurned());
        
        // 更新当日汇总
        updateDailySummary(exerciseRecord.getUserId(), exerciseRecord.getRecordDate());
        
        return exerciseRecord;
    }
    
    @Override
    public CalorieTrackingDTO getDailyTracking(Long userId, LocalDate date) {
        // 先尝试从汇总表获取
        DailySummary summary = dailySummaryMapper.findByUserIdAndDate(userId, date);
        if (summary != null) {
            return convertToDTO(summary);
        }
        
        // 如果没有汇总记录，则实时计算
        return calculateRealTimeTracking(userId, date);
    }
    
    @Override
    @Transactional
    public DailySummary updateDailySummary(Long userId, LocalDate date) {
        // 计算当日数据
        Integer totalCaloriesConsumed = foodRecordMapper.sumCaloriesByUserIdAndDate(userId, date);
        Double totalCaloriesBurned = exerciseRecordMapper.sumCaloriesBurnedByUserIdAndDate(userId, date);
        int netCalories = totalCaloriesConsumed - totalCaloriesBurned.intValue();
        
        // 获取用户热量目标
        User user = userMapper.selectById(userId);
        int calorieGoal = user != null ? user.getDailyCalorieGoal() : 2000; // 默认2000卡
        
        // 计算与目标的差值
        int calorieDifference = calorieGoal - netCalories;
        String goalStatus = calculateGoalStatus(netCalories, calorieGoal);
        
        // 创建或更新汇总记录
        DailySummary summary = dailySummaryMapper.findByUserIdAndDate(userId, date);
        if (summary == null) {
            summary = new DailySummary();
            summary.setUserId(userId);
            summary.setRecordDate(date);
        }
        
        summary.setTotalCaloriesConsumed(totalCaloriesConsumed);
        summary.setTotalCaloriesBurned(totalCaloriesBurned.intValue());
        summary.setNetCalories(netCalories);
        summary.setCalorieGoal(calorieGoal);
        summary.setCalorieDifference(calorieDifference);
        summary.setGoalStatus(goalStatus);
        
        if (summary.getId() == null) {
            dailySummaryMapper.insert(summary);
        } else {
            dailySummaryMapper.updateById(summary);
        }
        
        return summary;
    }
    
    @Override
    public List<CalorieTrackingDTO> getRecentTracking(Long userId, int days) {
        List<CalorieTrackingDTO> result = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 0; i < days; i++) {
            LocalDate date = currentDate.minusDays(i);
            CalorieTrackingDTO tracking = getDailyTracking(userId, date);
            result.add(tracking);
        }
        
        return result;
    }
    
    private CalorieTrackingDTO calculateRealTimeTracking(Long userId, LocalDate date) {
        CalorieTrackingDTO dto = new CalorieTrackingDTO();
        dto.setUserId(userId);
        dto.setDate(date);
        
        // 实时计算各项数据
        Integer consumed = foodRecordMapper.sumCaloriesByUserIdAndDate(userId, date);
        Double burned = exerciseRecordMapper.sumCaloriesBurnedByUserIdAndDate(userId, date);
        
        dto.setTotalCaloriesConsumed(consumed);
        dto.setTotalCaloriesBurned(burned.intValue());
        dto.setNetCalories(consumed - burned.intValue());
        
        // 获取用户目标
        User user = userMapper.selectById(userId);
        int goal = user != null ? user.getDailyCalorieGoal() : 2000;
        dto.setCalorieGoal(goal);
        dto.setCalorieDifference(goal - dto.getNetCalories());
        dto.setGoalStatus(calculateGoalStatus(dto.getNetCalories(), goal));
        
        return dto;
    }
    
    private CalorieTrackingDTO convertToDTO(DailySummary summary) {
        CalorieTrackingDTO dto = new CalorieTrackingDTO();
        dto.setUserId(summary.getUserId());
        dto.setDate(summary.getRecordDate());
        dto.setTotalCaloriesConsumed(summary.getTotalCaloriesConsumed());
        dto.setTotalCaloriesBurned(summary.getTotalCaloriesBurned());
        dto.setNetCalories(summary.getNetCalories());
        dto.setCalorieGoal(summary.getCalorieGoal());
        dto.setCalorieDifference(summary.getCalorieDifference());
        dto.setGoalStatus(summary.getGoalStatus());
        return dto;
    }
    
    private String calculateGoalStatus(int netCalories, int goal) {
        int difference = goal - netCalories;
        if (Math.abs(difference) <= goal * 0.1) { // ±10%以内算达成
            return "MEET";
        } else if (difference > 0) {
            return "UNDER"; // 摄入不足
        } else {
            return "OVER";  // 摄入超量
        }
    }
}