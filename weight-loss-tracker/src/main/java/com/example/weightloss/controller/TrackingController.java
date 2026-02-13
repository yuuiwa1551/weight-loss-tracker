package com.example.weightloss.controller;

import com.example.weightloss.common.Result;
import com.example.weightloss.dto.CalorieTrackingDTO;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.service.TrackingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*")
public class TrackingController {
    
    @Autowired
    private TrackingService trackingService;
    
    /**
     * 记录食物摄入
     */
    @PostMapping("/food")
    public Result<FoodRecord> recordFood(@Valid @RequestBody FoodRecord foodRecord) {
        try {
            FoodRecord result = trackingService.recordFood(foodRecord);
            return Result.success("食物记录成功", result);
        } catch (Exception e) {
            log.error("记录食物失败", e);
            return Result.error("记录食物失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录运动消耗
     */
    @PostMapping("/exercise")
    public Result<ExerciseRecord> recordExercise(@Valid @RequestBody ExerciseRecord exerciseRecord) {
        try {
            ExerciseRecord result = trackingService.recordExercise(exerciseRecord);
            return Result.success("运动记录成功", result);
        } catch (Exception e) {
            log.error("记录运动失败", e);
            return Result.error("记录运动失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取某日热量追踪数据
     */
    @GetMapping("/daily/{userId}")
    public Result<CalorieTrackingDTO> getDailyTracking(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            CalorieTrackingDTO result = trackingService.getDailyTracking(userId, date);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取日追踪数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取近期追踪数据
     */
    @GetMapping("/recent/{userId}")
    public Result<List<CalorieTrackingDTO>> getRecentTracking(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<CalorieTrackingDTO> result = trackingService.getRecentTracking(userId, days);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取近期数据失败", e);
            return Result.error("获取数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动更新某日汇总
     */
    @PostMapping("/summary/{userId}")
    public Result<String> updateDailySummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            trackingService.updateDailySummary(userId, date);
            return Result.success("汇总更新成功", "success");
        } catch (Exception e) {
            log.error("更新汇总失败", e);
            return Result.error("更新汇总失败: " + e.getMessage());
        }
    }
}