package com.example.weightloss.learning;

import com.example.weightloss.entity.DailyCalorieRecord;
import com.example.weightloss.mapper.DailyCalorieRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 学习用的简单Service示例
 * 展示完整的CRUD操作流程
 */
@Service
public class LearningExampleService {
    
    @Autowired
    private DailyCalorieRecordMapper calorieRecordMapper;
    
    /**
     * 1. 查询所有饮食记录 - READ操作
     */
    public List<DailyCalorieRecord> findAllRecords() {
        System.out.println("=== 查询所有记录 ===");
        List<DailyCalorieRecord> records = calorieRecordMapper.selectList(null);
        System.out.println("查询到 " + records.size() + " 条记录");
        return records;
    }
    
    /**
     * 2. 根据ID查询单条记录 - READ操作
     */
    public DailyCalorieRecord findRecordById(Long id) {
        System.out.println("=== 查询ID为 " + id + " 的记录 ===");
        DailyCalorieRecord record = calorieRecordMapper.selectById(id);
        if (record != null) {
            System.out.println("找到记录: " + record.getFoodName());
        } else {
            System.out.println("未找到该记录");
        }
        return record;
    }
    
    /**
     * 3. 插入新记录 - CREATE操作
     */
    public boolean insertRecord(String foodName, BigDecimal calories) {
        System.out.println("=== 插入新记录 ===");
        DailyCalorieRecord record = new DailyCalorieRecord();
        record.setRecordDate(LocalDate.now());
        record.setFoodName(foodName);
        record.setSingleCalorie(calories);
        record.setMealTime("午餐");
        record.setMealType(2); // 2表示午餐
        
        int result = calorieRecordMapper.insert(record);
        boolean success = result > 0;
        System.out.println("插入" + (success ? "成功" : "失败"));
        return success;
    }
    
    /**
     * 4. 更新记录 - UPDATE操作
     */
    public boolean updateRecord(Long id, String newFoodName) {
        System.out.println("=== 更新记录 ===");
        DailyCalorieRecord record = calorieRecordMapper.selectById(id);
        if (record != null) {
            record.setFoodName(newFoodName);
            int result = calorieRecordMapper.updateById(record);
            boolean success = result > 0;
            System.out.println("更新" + (success ? "成功" : "失败"));
            return success;
        }
        System.out.println("记录不存在，无法更新");
        return false;
    }
    
    /**
     * 5. 删除记录 - DELETE操作
     */
    public boolean deleteRecord(Long id) {
        System.out.println("=== 删除记录 ===");
        int result = calorieRecordMapper.deleteById(id);
        boolean success = result > 0;
        System.out.println("删除" + (success ? "成功" : "失败"));
        return success;
    }
    
    /**
     * 6. 综合演示 - 完整的CRUD流程
     */
    public void demonstrateCRUD() {
        System.out.println("\n=== 开始CRUD演示 ===");
        
        // 1. 插入测试数据
        insertRecord("苹果", new BigDecimal("52.00"));
        insertRecord("香蕉", new BigDecimal("89.00"));
        
        // 2. 查询所有记录
        findAllRecords();
        
        // 3. 查询第一条记录
        DailyCalorieRecord firstRecord = findRecordById(1L);
        
        // 4. 更新记录
        if (firstRecord != null) {
            updateRecord(firstRecord.getId(), "红富士苹果");
        }
        
        // 5. 再次查询验证更新
        findRecordById(1L);
        
        System.out.println("=== CRUD演示结束 ===\n");
    }
}