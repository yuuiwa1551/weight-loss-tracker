package com.example.weightloss;

import com.example.weightloss.entity.DailyCalorieRecord;
import com.example.weightloss.mapper.DailyCalorieRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库连接测试类
 * 项目启动时会自动运行这个测试
 */
@Component
public class DatabaseConnectionTest implements CommandLineRunner {
    
    @Autowired
    private DailyCalorieRecordMapper dailyCalorieRecordMapper;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 数据库连接测试 ===");
        
        try {
            // 测试查询所有饮食记录
            List<DailyCalorieRecord> records = dailyCalorieRecordMapper.selectList(null);
            
            if (!records.isEmpty()) {
                System.out.println("✅ 数据库连接成功！");
                System.out.println("找到 " + records.size() + " 条饮食记录");
                System.out.println("最新记录: " + records.get(0).getFoodName() + 
                                 ", 热量: " + records.get(0).getSingleCalorie());
            } else {
                System.out.println("⚠️  没有找到饮食记录，但连接正常");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 测试结束 ===");
    }
}