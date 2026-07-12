# API 契约

后端默认地址：`http://localhost:8080`

统一响应格式：

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

错误响应也使用同一结构，HTTP 状态码表达错误类型。

当前接口模块：

- Users：QQ 用户解析和本机用户列表
- Profile：用户目标资料
- Food Records：食物摄入记录
- Exercise Records：运动消耗记录
- Weight Records：体重趋势记录
- Reports：7 天和 30 天周期报表
- Summaries：每日汇总和最近趋势

除用户解析和用户列表外，业务接口均使用 `/api/users/{userId}` 前缀。`userId` 是后端内部 ID，AstrBot 先通过用户解析接口把 QQ 号转换为该 ID。

## Users

### 解析 QQ 用户

```http
POST /api/users/resolve
Content-Type: application/json
```

请求体：

```json
{
  "platform": "aiocqhttp",
  "username": "1154824108",
  "displayName": "雪"
}
```

`username` 必须是纯数字 QQ 号。相同 `platform + username` 重复解析会返回同一个用户，并更新非空显示昵称。

### 用户列表

```http
GET /api/users
```

该接口供仅绑定本机地址的 Web 管理界面使用。

## Profile

### 获取资料

```http
GET /api/users/{userId}/profile
```

响应 `data`：

```json
{
  "id": 1,
  "nickname": "Demo User",
  "heightCm": 175.0,
  "currentWeightKg": 75.0,
  "targetWeightKg": 68.0,
  "dailyCalorieGoal": 1900,
  "bmi": 24.5,
  "weightToLoseKg": 7.0,
  "profileComplete": true,
  "missingFields": [],
  "createdAt": "2026-06-08T10:00:00",
  "updatedAt": "2026-06-08T10:00:00"
}
```

### 更新资料

```http
PUT /api/users/{userId}/profile
Content-Type: application/json
```

请求体：

```json
{
  "nickname": "Demo User",
  "heightCm": 175.0,
  "currentWeightKg": 75.0,
  "targetWeightKg": 68.0,
  "dailyCalorieGoal": 1900
}
```

所有资料字段均可为空，以支持 QQ 用户渐进建档。非空时的数值范围：`heightCm` 为 `50.0` 到 `250.0`，`currentWeightKg` 和 `targetWeightKg` 为 `20.0` 到 `500.0`，`dailyCalorieGoal` 为 `500` 到 `10000`。

## Food Records

### 新增食物记录

```http
POST /api/users/{userId}/food-records
Content-Type: application/json
```

请求体：

```json
{
  "recordDate": "2026-06-08",
  "mealType": "LUNCH",
  "foodName": "Chicken salad",
  "calories": 460,
  "protein": 38.0,
  "fat": 18.0,
  "carbohydrate": 28.0,
  "note": "optional",
  "source": "ASTRBOT",
  "clientRequestId": "aiocqhttp:message-id:food",
  "nutritionSource": "LLM_ESTIMATE",
  "estimationNote": "Estimated from one serving"
}
```

`mealType` 可选值：`BREAKFAST`、`LUNCH`、`DINNER`、`SNACK`。
数值范围：`calories` 为 `0` 到 `20000`，`protein`、`fat`、`carbohydrate` 为 `0.0` 到 `1000.0`。
`source` 默认为 `WEB`；`nutritionSource` 默认为 `USER_PROVIDED`。同一用户重复提交相同的非空 `clientRequestId` 时返回已有记录。

### 查询某日食物记录

```http
GET /api/users/{userId}/food-records?date=2026-06-08
```

不传 `date` 时默认查询今天。

### 删除食物记录

```http
DELETE /api/users/{userId}/food-records/{id}
```

## Exercise Records

### 新增运动记录

```http
POST /api/users/{userId}/exercise-records
Content-Type: application/json
```

请求体：

```json
{
  "recordDate": "2026-06-08",
  "exerciseType": "Cardio",
  "exerciseName": "Easy run",
  "durationMinutes": 30,
  "caloriesBurned": 280,
  "note": "optional",
  "source": "ASTRBOT",
  "clientRequestId": "aiocqhttp:message-id:exercise"
}
```

数值范围：`durationMinutes` 为 `1` 到 `1440`，`caloriesBurned` 为 `0` 到 `10000`。

### 查询某日运动记录

```http
GET /api/users/{userId}/exercise-records?date=2026-06-08
```

不传 `date` 时默认查询今天。

### 删除运动记录

```http
DELETE /api/users/{userId}/exercise-records/{id}
```

## Weight Records

### 新增体重记录

```http
POST /api/users/{userId}/weight-records
Content-Type: application/json
```

请求体：

```json
{
  "recordDate": "2026-06-08",
  "weightKg": 74.2,
  "bodyFatPercentage": 23.4,
  "note": "optional",
  "source": "ASTRBOT",
  "clientRequestId": "aiocqhttp:message-id:weight"
}
```

`bodyFatPercentage` 和 `note` 可为空。
数值范围：`weightKg` 为 `1.0` 到 `500.0`，`bodyFatPercentage` 为 `0.0` 到 `100.0`。
新增或删除体重记录后，资料中的 `currentWeightKg` 会同步为剩余记录中的最新值。

### 查询近期体重记录

```http
GET /api/users/{userId}/weight-records/recent?days=30
```

`days` 范围是 `7` 到 `365`，默认 `30`。超出范围会返回 `400` 校验错误。

响应 `data`：

```json
[
  {
    "id": 1,
    "recordDate": "2026-06-08",
    "weightKg": 74.2,
    "bodyFatPercentage": 23.4,
    "note": "optional",
    "source": "ASTRBOT",
    "clientRequestId": "aiocqhttp:message-id:weight",
    "createdAt": "2026-06-08T10:00:00",
    "updatedAt": "2026-06-08T10:00:00"
  }
]
```

### 删除体重记录

```http
DELETE /api/users/{userId}/weight-records/{id}
```

## Reports

### 周期报表

```http
GET /api/users/{userId}/reports/overview?days=7
```

`days` 范围是 `7` 到 `365`。前端当前使用 `7` 作为周报，`30` 作为月报。

响应 `data`：

```json
{
  "startDate": "2026-06-03",
  "endDate": "2026-06-09",
  "days": 7,
  "totalCaloriesConsumed": 780,
  "totalCaloriesBurned": 280,
  "netCalories": 500,
  "averageCaloriesConsumed": 111.4,
  "averageCaloriesBurned": 40.0,
  "averageNetCalories": 71.4,
  "averageProtein": 7.1,
  "averageFat": 3.6,
  "averageCarbohydrate": 11.4,
  "dailyCalorieGoal": 1900,
  "daysUnderGoal": 7,
  "daysMeetGoal": 0,
  "daysOverGoal": 0,
  "startWeightKg": 74.8,
  "endWeightKg": 75.0,
  "weightChangeKg": 0.2,
  "dailySummaries": []
}
```

## Summaries

### 每日汇总

```http
GET /api/users/{userId}/summaries/daily?date=2026-06-08
```

不传 `date` 时默认查询今天。

响应 `data`：

```json
{
  "date": "2026-06-08",
  "totalCaloriesConsumed": 780,
  "totalCaloriesBurned": 280,
  "netCalories": 500,
  "dailyCalorieGoal": 1900,
  "calorieDifference": 1400,
  "goalStatus": "UNDER",
  "totalProtein": 50.0,
  "totalFat": 25.0,
  "totalCarbohydrate": 80.0,
  "foodRecords": [],
  "exerciseRecords": []
}
```

`goalStatus` 可选值：`UNSET`、`UNDER`、`MEET`、`OVER`。用户未设置每日热量目标时，`dailyCalorieGoal` 和 `calorieDifference` 为 `null`，状态为 `UNSET`。

### 最近趋势

```http
GET /api/users/{userId}/summaries/recent?days=7
```

`days` 范围是 `1` 到 `90`，默认 `7`。

## Phase 6 Energy Contract

以下契约已在 Phase 6 第 1 阶段冻结，但 Controller 将在后续阶段逐步启用。精确计算规则见 [ENERGY_CALCULATION.md](ENERGY_CALCULATION.md)。

### 资料扩展

现有 Profile 请求和响应将增加：

```json
{
  "ageYears": 24,
  "formulaSex": "FEMALE",
  "nonExerciseActivityLevel": "LIGHT",
  "calorieGoalMode": "AUTO"
}
```

- `formulaSex`：`MALE`、`FEMALE`
- `nonExerciseActivityLevel`：`SEDENTARY`、`LIGHT`、`MODERATE`、`HIGH`
- `calorieGoalMode`：`UNSET`、`MANUAL`、`AUTO`

### 计划预览

```http
POST /api/users/{userId}/energy-plans/preview
Content-Type: application/json
```

请求支持三种缺口来源，两个字段不能同时提供：

```json
{
  "dailyDeficitCalories": 450,
  "targetPeriodDays": null
}
```

- 只传 `dailyDeficitCalories`：`EXPLICIT`
- 只传 `targetPeriodDays`：结合 Profile 中的目标体重计算 `TARGET_PERIOD`
- 两者都不传：使用 `DEFAULT_RATE`

响应 `data`：

```json
{
  "calculation": {
    "calculationMethod": "MIFFLIN_ST_JEOR",
    "calculationVersion": "P6_V1",
    "deficitMode": "EXPLICIT",
    "ageYears": 24,
    "formulaSex": "FEMALE",
    "heightCm": 165.0,
    "weightKg": 52.0,
    "targetWeightKg": 48.0,
    "nonExerciseActivityLevel": "LIGHT",
    "targetPeriodDays": null,
    "restingEnergyCalories": 1270,
    "baselineExpenditureCalories": 1651,
    "dailyDeficitCalories": 450,
    "baseIntakeTargetCalories": 1201,
    "profileUpdatedAt": "2026-07-12T12:00:00"
  },
  "previewFingerprint": "sha256-hex"
}
```

预览不写入业务表。

### 确认计划

```http
POST /api/users/{userId}/energy-plans
Content-Type: application/json
```

```json
{
  "calculation": {
    "dailyDeficitCalories": 450,
    "targetPeriodDays": null
  },
  "previewFingerprint": "sha256-hex",
  "clientRequestId": "astrbot:1154824108:energy-plan:message-id"
}
```

后端重新计算并核对指纹。预览过期返回 `409 Conflict`，不写入计划。相同用户重复提交相同 `clientRequestId` 时返回已有计划。

### 当前计划

```http
GET /api/users/{userId}/energy-plans/active
```

响应包含计划 ID、计算快照、状态、生效日期、幂等键和时间戳。创建新计划后，旧计划状态变为 `SUPERSEDED`。

### 食物预览

```http
POST /api/users/{userId}/food-records/preview
Content-Type: application/json
```

请求体与新增食物记录一致。响应返回规范化后的全部食物数值、写入后的 `projectedEnergyBudget` 和 `previewFingerprint`，但不写数据库。

正式调用 `POST /api/users/{userId}/food-records` 时必须回传最近一次 `previewFingerprint`。该字段在 Phase 6 原子写入阶段启用强制校验。

### 运动预览

```http
POST /api/users/{userId}/exercise-records/preview
Content-Type: application/json
```

请求体与新增运动记录一致。响应返回运动时长、消耗热量、写入后的 `projectedEnergyBudget` 和 `previewFingerprint`，但不写数据库。

正式调用 `POST /api/users/{userId}/exercise-records` 时必须回传最近一次 `previewFingerprint`。用户明确提供或设备提供的数值也不能绕过预览。

### 每日能量预算

```http
GET /api/users/{userId}/energy-budgets/daily?date=2026-07-12
```

响应 `data`：

```json
{
  "date": "2026-07-12",
  "restingEnergyCalories": 1270,
  "baselineExpenditureCalories": 1651,
  "exerciseCaloriesBurned": 280,
  "estimatedTotalExpenditureCalories": 1931,
  "baseIntakeTargetCalories": 1201,
  "todayIntakeBudgetCalories": 1481,
  "caloriesConsumed": 1100,
  "remainingIntakeCalories": 381,
  "projectedDeficitCalories": 831,
  "goalMode": "AUTO",
  "calculationMethod": "MIFFLIN_ST_JEOR",
  "calculationVersion": "P6_V1"
}
```

当日预算是已确认计划、食物和运动的实时推导结果，不单独保存冗余快照。计划、食物或运动的预览指纹必须包含相关当日状态；确认时状态变化则返回 `409 Conflict`。
