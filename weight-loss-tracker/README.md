# 减肥追踪系统 Demo

## 项目介绍

感觉不如蚂蚁阿福。。。。。

这是一个基于 Spring Boot + MyBatis-Plus 的减肥追踪系统后端 demo，主要演示热量记录、运动追踪、每日汇总和 AI 食物分析预留能力。当前版本适合作为功能原型继续扩展，支持：
- 📊 热量摄入记录和统计
- 🏃‍♂️ 运动消耗追踪
- 🎯 个人减肥目标管理
- 🤖 AI食物营养分析（预留接口）

## 新版重写说明

当前仓库已经新增 `weight-loss-tracker-v2` 作为后续演进主线。V2 使用 Spring Boot 4 + React + TypeScript 重写，已经实现前后端联调、食物记录、运动记录、体重趋势、7 天/30 天周期报表和目标资料页面。

如果继续开发，建议优先查看 `../weight-loss-tracker-v2/README.md`、`../weight-loss-tracker-v2/docs/SPEC.md`、`../weight-loss-tracker-v2/docs/PLAN.md` 和 `../weight-loss-tracker-v2/docs/API.md`。

## 技术栈

- **Spring Boot 2.7.0** - 核心框架
- **MyBatis-Plus 3.5.2** - ORM框架
- **MySQL** - 数据库
- **Lombok** - 简化代码
- **OpenAI API** - AI食物分析（可选）

## 项目结构

```
src/main/java/com/example/weightloss/
├── WeightLossApplication.java      # 启动类
├── controller/                     # 控制器层
│   ├── TrackingController.java    # 追踪相关API
│   └── RecordController.java      # 记录管理API
├── service/                       # 服务层
│   ├── TrackingService.java       # 追踪服务接口
│   └── impl/                      # 服务实现
│       ├── TrackingServiceImpl.java
│       └── UserServiceImpl.java
├── mapper/                        # 数据访问层
│   ├── UserMapper.java
│   ├── FoodRecordMapper.java
│   ├── ExerciseRecordMapper.java
│   └── DailySummaryMapper.java
├── entity/                        # 实体类
│   ├── User.java
│   ├── FoodRecord.java
│   ├── ExerciseRecord.java
│   └── DailySummary.java
├── dto/                           # 数据传输对象
│   └── CalorieTrackingDTO.java
├── common/                        # 通用类
│   └── Result.java                # 统一响应结果
└── ai/                            # AI相关
    ├── dto/                       # AI数据传输对象
    ├── service/                   # AI服务接口
    └── task/                      # AI定时任务
```

## 快速开始

### 1. 数据库配置

1. 执行 `sql/init.sql` 初始化数据库
2. 修改 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weight_loss_tracker
    username: your_username
    password: your_password
```

### 2. 启动项目

```bash
# Maven方式
mvn spring-boot:run

# 或者直接运行主类
java -jar target/weight-loss-tracker-1.0.0.jar
```

### 3. 测试API

项目启动后访问：`http://localhost:8080`

## API接口文档

### 🔥 核心追踪接口

#### 记录食物摄入
```
POST /api/tracking/food
{
    "userId": 1,
    "recordDate": "2024-01-15",
    "foodName": "燕麦粥",
    "foodDescription": "早餐燕麦粥一碗",
    "calories": 150,
    "protein": 5.0,
    "fat": 3.0,
    "carbohydrate": 25.0,
    "mealType": "BREAKFAST"
}
```

#### 记录运动消耗
```
POST /api/tracking/exercise
{
    "userId": 1,
    "recordDate": "2024-01-15",
    "exerciseType": "有氧运动",
    "exerciseName": "跑步30分钟",
    "duration": 30,
    "caloriesBurned": 300.0,
    "intensity": "MEDIUM"
}
```

#### 获取日追踪数据
```
GET /api/tracking/daily/1?date=2024-01-15
```

#### 获取近期数据
```
GET /api/tracking/recent/1?days=7
```

### 📋 记录管理接口

#### 分页查询食物记录
```
GET /api/records/food/1?pageNum=1&pageSize=10&startDate=2024-01-01&endDate=2024-01-15
```

#### 分页查询运动记录
```
GET /api/records/exercise/1?pageNum=1&pageSize=10
```

## AI功能说明

### 已预留的功能

1. **AI食物分析接口** - `AIFoodAnalysisService`
2. **定时分析任务** - 自动处理未分析的食物记录
3. **OpenAI集成示例** - `OpenAIFoodAnalysisServiceImpl`

### 启用AI功能

1. 在 `application.yml` 添加OpenAI配置：
```yaml
openai:
  api:
    key: your-openai-api-key
    url: https://api.openai.com/v1/chat/completions
```

2. AI会自动分析用户录入的食物描述，并填充详细的营养信息

## 后续更新方向

当前项目可以作为减肥追踪产品的后端 demo。后续如果继续完善，可以从下面几个方向逐步更新：

1. **以 V2 为主线继续演进**：当前 V2 已补齐前后端联调、仪表盘、记录录入、体重趋势和周期报表。
2. **补齐用户体系**：增加登录鉴权、用户资料维护、目标调整和多用户数据隔离。
3. **增强数据分析**：继续完善折线图、营养结构占比、目标达成分析和长期体重趋势。
4. **落地 AI 能力**：接入食物识别、营养估算、饮食建议和运动计划推荐。
5. **提升工程完整度**：补充接口文档、自动化测试、异常处理、参数校验和部署配置。
