package com.example.weightloss.learning;

import com.example.weightloss.common.Result;
import com.example.weightloss.entity.DailyCalorieRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 学习用的Controller示例
 * 提供简单的API接口用于练习
 */
@RestController
@RequestMapping("/api/learn")
public class LearningController {
    
    @Autowired
    private LearningExampleService learningService;
    
    /**
     * 演示完整的CRUD操作
     * GET /api/learn/demo
     */
    @GetMapping("/demo")
    public Result<String> demonstrateCRUD() {
        try {
            learningService.demonstrateCRUD();
            return Result.success("CRUD演示完成，请查看控制台输出");
        } catch (Exception e) {
            return Result.error("演示出错: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有饮食记录
     * GET /api/learn/records
     */
    @GetMapping("/records")
    public Result<List<DailyCalorieRecord>> getAllRecords() {
        try {
            List<DailyCalorieRecord> records = learningService.findAllRecords();
            return Result.success(records);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询记录
     * GET /api/learn/records/{id}
     */
    @GetMapping("/records/{id}")
    public Result<DailyCalorieRecord> getRecordById(@PathVariable Long id) {
        try {
            DailyCalorieRecord record = learningService.findRecordById(id);
            if (record != null) {
                return Result.success(record);
            } else {
                return Result.error("记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加新记录
     * POST /api/learn/records
     */
    @PostMapping("/records")
    public Result<String> addRecord(
            @RequestParam String foodName,
            @RequestParam BigDecimal calories) {
        try {
            boolean success = learningService.insertRecord(foodName, calories);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            return Result.error("添加出错: " + e.getMessage());
        }
    }
    
    /**
     * 更新记录
     * PUT /api/learn/records/{id}
     */
    @PutMapping("/records/{id}")
    public Result<String> updateRecord(
            @PathVariable Long id,
            @RequestParam String foodName) {
        try {
            boolean success = learningService.updateRecord(id, foodName);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新出错: " + e.getMessage());
        }
    }
    
    /**
     * 删除记录
     * DELETE /api/learn/records/{id}
     */
    @DeleteMapping("/records/{id}")
    public Result<String> deleteRecord(@PathVariable Long id) {
        try {
            boolean success = learningService.deleteRecord(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除出错: " + e.getMessage());
        }
    }
}