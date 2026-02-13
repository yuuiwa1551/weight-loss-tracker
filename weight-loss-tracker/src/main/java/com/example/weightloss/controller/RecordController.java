package com.example.weightloss.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weightloss.common.Result;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.mapper.FoodRecordMapper;
import com.example.weightloss.mapper.ExerciseRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class RecordController {
    
    @Autowired
    private FoodRecordMapper foodRecordMapper;
    
    @Autowired
    private ExerciseRecordMapper exerciseRecordMapper;
    
    /**
     * 分页查询食物记录
     */
    @GetMapping("/food/{userId}")
    public Result<Page<FoodRecord>> getFoodRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            Page<FoodRecord> page = new Page<>(pageNum, pageSize);
            
            if (startDate != null && endDate != null) {
                Page<FoodRecord> result = foodRecordMapper.findByUserIdAndDateRange(page, userId, startDate, endDate);
                return Result.success(result);
            } else {
                QueryWrapper<FoodRecord> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userId)
                       .eq("deleted", 0)
                       .orderByDesc("record_date", "create_time");
                Page<FoodRecord> result = foodRecordMapper.selectPage(page, wrapper);
                return Result.success(result);
            }
        } catch (Exception e) {
            log.error("查询食物记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询运动记录
     */
    @GetMapping("/exercise/{userId}")
    public Result<Page<ExerciseRecord>> getExerciseRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            Page<ExerciseRecord> page = new Page<>(pageNum, pageSize);
            
            if (startDate != null && endDate != null) {
                Page<ExerciseRecord> result = exerciseRecordMapper.findByUserIdAndDateRange(page, userId, startDate, endDate);
                return Result.success(result);
            } else {
                QueryWrapper<ExerciseRecord> wrapper = new QueryWrapper<>();
                wrapper.eq("user_id", userId)
                       .eq("deleted", 0)
                       .orderByDesc("record_date", "create_time");
                Page<ExerciseRecord> result = exerciseRecordMapper.selectPage(page, wrapper);
                return Result.success(result);
            }
        } catch (Exception e) {
            log.error("查询运动记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除食物记录
     */
    @DeleteMapping("/food/{id}")
    public Result<String> deleteFoodRecord(@PathVariable Long id) {
        try {
            foodRecordMapper.deleteById(id);
            return Result.success("删除成功", "success");
        } catch (Exception e) {
            log.error("删除食物记录失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除运动记录
     */
    @DeleteMapping("/exercise/{id}")
    public Result<String> deleteExerciseRecord(@PathVariable Long id) {
        try {
            exerciseRecordMapper.deleteById(id);
            return Result.success("删除成功", "success");
        } catch (Exception e) {
            log.error("删除运动记录失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}