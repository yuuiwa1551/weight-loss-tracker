-- 减肥追踪系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS weight_loss_tracker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE weight_loss_tracker;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) COMMENT '邮箱',
    password VARCHAR(255) COMMENT '密码',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别(MALE/FEMALE)',
    height DOUBLE COMMENT '身高(cm)',
    weight DOUBLE COMMENT '当前体重(kg)',
    target_weight DOUBLE COMMENT '目标体重(kg)',
    daily_calorie_goal INT DEFAULT 2000 COMMENT '每日热量目标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识'
) COMMENT '用户表';

-- 食物记录表
CREATE TABLE food_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    food_name VARCHAR(100) COMMENT '食物名称',
    food_description TEXT COMMENT '食物详细描述',
    calories INT COMMENT '热量(卡路里)',
    protein DOUBLE COMMENT '蛋白质(g)',
    fat DOUBLE COMMENT '脂肪(g)',
    carbohydrate DOUBLE COMMENT '碳水化合物(g)',
    meal_type VARCHAR(20) COMMENT '餐次(BREAKFAST/LUNCH/DINNER/SNACK)',
    ai_analysis TEXT COMMENT 'AI分析结果',
    ai_processed BOOLEAN DEFAULT FALSE COMMENT '是否已AI处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_create_time (create_time)
) COMMENT '食物记录表';

-- 运动记录表
CREATE TABLE exercise_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    exercise_type VARCHAR(50) COMMENT '运动类型',
    exercise_name VARCHAR(100) COMMENT '具体运动名称',
    duration INT COMMENT '持续时间(分钟)',
    calories_burned DOUBLE COMMENT '消耗热量',
    intensity VARCHAR(20) COMMENT '运动强度(LOW/MEDIUM/HIGH)',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_create_time (create_time)
) COMMENT '运动记录表';

-- 每日汇总表
CREATE TABLE daily_summaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    total_calories_consumed INT DEFAULT 0 COMMENT '总摄入热量',
    total_calories_burned INT DEFAULT 0 COMMENT '总消耗热量',
    net_calories INT DEFAULT 0 COMMENT '净热量',
    total_protein DOUBLE DEFAULT 0 COMMENT '总蛋白质',
    total_fat DOUBLE DEFAULT 0 COMMENT '总脂肪',
    total_carbohydrate DOUBLE DEFAULT 0 COMMENT '总碳水化合物',
    calorie_goal INT COMMENT '热量目标',
    calorie_difference INT COMMENT '与目标差值',
    goal_status VARCHAR(20) COMMENT '目标状态(UNDER/OVER/MEET)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标识',
    UNIQUE KEY uk_user_date (user_id, record_date),
    INDEX idx_record_date (record_date)
) COMMENT '每日汇总表';

-- 插入示例数据
INSERT INTO users (username, email, age, gender, height, weight, target_weight, daily_calorie_goal) VALUES
('test_user', 'test@example.com', 25, 'MALE', 175.0, 75.0, 70.0, 2200);

INSERT INTO food_records (user_id, record_date, food_name, food_description, calories, protein, fat, carbohydrate, meal_type) VALUES
(1, CURDATE(), '燕麦粥', '早餐燕麦粥一碗', 150, 5.0, 3.0, 25.0, 'BREAKFAST'),
(1, CURDATE(), '鸡胸肉沙拉', '午餐鸡胸肉配蔬菜沙拉', 350, 35.0, 12.0, 15.0, 'LUNCH');

INSERT INTO exercise_records (user_id, record_date, exercise_type, exercise_name, duration, calories_burned, intensity) VALUES
(1, CURDATE(), '有氧运动', '跑步30分钟', 30, 300.0, 'MEDIUM');