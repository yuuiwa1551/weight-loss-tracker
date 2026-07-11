# 产品规格说明

## 1. 项目定位

减肥追踪系统 V2 是一个本机运行的多用户减肥记录 demo。用户通过本地 Web 或 AstrBot 聊天记录饮食、运动和体重，系统按 QQ 用户隔离数据并自动汇总热量摄入、运动消耗、净热量、体重变化和目标完成情况。

当前版本优先追求：能跑、能联调、数据结构清楚、后续容易扩展。

## 2. MVP 范围

### 必须有

1. 用户目标配置
   - 昵称
   - 身高
   - 当前体重
   - 目标体重
   - 每日热量目标

2. 食物记录
   - 日期
   - 餐次：早餐、午餐、晚餐、加餐
   - 食物名称
   - 热量
   - 蛋白质、脂肪、碳水，可选
   - 备注，可选

3. 运动记录
   - 日期
   - 运动名称
   - 运动类型
   - 时长
   - 消耗热量
   - 备注，可选

4. 仪表盘
   - 今日摄入热量
   - 今日运动消耗
   - 今日净热量
   - 与目标差值
   - 今日食物列表
   - 今日运动列表
   - 最近 7 天趋势

5. 体重记录
   - 日期
   - 体重
   - 体脂率，可选
   - 备注，可选
   - 最近 30 天趋势

6. 周期报表
   - 7 天周报
   - 30 天月报
   - 摄入、消耗、净热量汇总
   - 目标达成天数
   - 营养均值
   - 体重变化

### 暂不做

1. 传统登录注册
2. 角色、组织和管理员权限体系
3. 好友、排行榜、社交功能
4. 医疗级 AI 分析
5. 复杂营养建议
6. 移动端 App

这些能力可以写在后续路线图里，但不要进入第一版代码。

## 3. 领域模型

### AppUser

AstrBot 用户使用 `platform + username` 唯一标识。第一期 `platform` 为 `aiocqhttp`，`username` 为发送者 QQ 号。

字段：

- id
- platform
- username
- displayName
- createdAt
- updatedAt

### UserProfile

每个 AppUser 对应一条可渐进完善的资料。

字段：

- id
- userId
- nickname
- heightCm
- currentWeightKg
- targetWeightKg
- dailyCalorieGoal
- createdAt
- updatedAt

### FoodRecord

字段：

- id
- userId
- recordDate
- mealType
- foodName
- calories
- protein
- fat
- carbohydrate
- note
- source
- clientRequestId
- nutritionSource
- estimationNote
- createdAt
- updatedAt

### ExerciseRecord

字段：

- id
- userId
- recordDate
- exerciseType
- exerciseName
- durationMinutes
- caloriesBurned
- note
- source
- clientRequestId
- createdAt
- updatedAt

### WeightRecord

字段：

- id
- userId
- recordDate
- weightKg
- bodyFatPercentage
- note
- source
- clientRequestId
- createdAt
- updatedAt

### DailySummary

DailySummary 可以实时计算，不一定要落库。第一版建议由后端接口实时聚合，减少同步和缓存问题。

字段：

- date
- totalCaloriesConsumed
- totalCaloriesBurned
- netCalories
- dailyCalorieGoal
- calorieDifference
- goalStatus
- foodRecords
- exerciseRecords

### PeriodReport

PeriodReport 由后端实时聚合，不单独落库。

字段：

- startDate
- endDate
- days
- totalCaloriesConsumed
- totalCaloriesBurned
- netCalories
- averageCaloriesConsumed
- averageCaloriesBurned
- averageNetCalories
- averageProtein
- averageFat
- averageCarbohydrate
- dailyCalorieGoal
- daysUnderGoal
- daysMeetGoal
- daysOverGoal
- startWeightKg
- endWeightKg
- weightChangeKg
- dailySummaries

## 4. API 契约草案

统一响应格式：

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

### 用户资料

```http
POST /api/users/resolve
GET /api/users
GET /api/users/{userId}/profile
PUT /api/users/{userId}/profile
```

### 食物记录

```http
POST /api/users/{userId}/food-records
GET /api/users/{userId}/food-records?date=2026-06-08
DELETE /api/users/{userId}/food-records/{id}
```

### 运动记录

```http
POST /api/users/{userId}/exercise-records
GET /api/users/{userId}/exercise-records?date=2026-06-08
DELETE /api/users/{userId}/exercise-records/{id}
```

### 体重记录

```http
POST /api/users/{userId}/weight-records
GET /api/users/{userId}/weight-records/recent?days=30
DELETE /api/users/{userId}/weight-records/{id}
```

### 汇总

```http
GET /api/users/{userId}/summaries/daily?date=2026-06-08
GET /api/users/{userId}/summaries/recent?days=7
```

### 报表

```http
GET /api/users/{userId}/reports/overview?days=7
GET /api/users/{userId}/reports/overview?days=30
```

## 5. 前端页面

### Dashboard

第一屏就是仪表盘，不做营销页。

包含：

- 日期选择
- 热量汇总卡片
- 目标状态
- 今日食物记录
- 今日运动记录
- 7 天趋势

### Food Records

包含：

- 新增食物表单
- 当天食物记录列表
- 删除记录

### Exercise Records

包含：

- 新增运动表单
- 当天运动记录列表
- 删除记录

### Weight Trend

包含：

- 新增体重记录表单
- 最近 30 天体重趋势
- 体重记录列表
- 删除记录

### Reports

包含：

- 7 天周报
- 30 天月报
- 热量、目标达成和营养均值
- 体重变化

### Profile

包含：

- 用户基础目标配置
- BMI 简单展示

## 6. 验收标准

第一版 demo 完成时，需要满足：

1. 后端能启动
2. 前端能启动
3. 前端可以创建食物记录
4. 前端可以创建运动记录
5. Dashboard 能显示当天汇总
6. 最近 7 天接口能返回趋势数据
7. 体重记录能新增、展示和删除
8. 周期报表能返回 7 天和 30 天统计
9. README 里的启动步骤可复现
10. 相同 QQ 在私聊和群聊中映射为同一用户
11. 不同 QQ 的资料、记录、汇总和报表互相隔离
12. 重复的 AstrBot 请求不会重复写入记录
