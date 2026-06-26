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

- Profile：单用户目标资料
- Food Records：食物摄入记录
- Exercise Records：运动消耗记录
- Weight Records：体重趋势记录
- Reports：7 天和 30 天周期报表
- Summaries：每日汇总和最近趋势

## Profile

### 获取资料

```http
GET /api/profile
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
  "createdAt": "2026-06-08T10:00:00",
  "updatedAt": "2026-06-08T10:00:00"
}
```

### 更新资料

```http
PUT /api/profile
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

数值范围：`heightCm` 为 `50.0` 到 `250.0`，`currentWeightKg` 和 `targetWeightKg` 为 `20.0` 到 `500.0`，`dailyCalorieGoal` 为 `500` 到 `10000`。

## Food Records

### 新增食物记录

```http
POST /api/food-records
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
  "note": "optional"
}
```

`mealType` 可选值：`BREAKFAST`、`LUNCH`、`DINNER`、`SNACK`。
数值范围：`calories` 为 `0` 到 `20000`，`protein`、`fat`、`carbohydrate` 为 `0.0` 到 `1000.0`。

### 查询某日食物记录

```http
GET /api/food-records?date=2026-06-08
```

不传 `date` 时默认查询今天。

### 删除食物记录

```http
DELETE /api/food-records/{id}
```

## Exercise Records

### 新增运动记录

```http
POST /api/exercise-records
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
  "note": "optional"
}
```

数值范围：`durationMinutes` 为 `1` 到 `1440`，`caloriesBurned` 为 `0` 到 `10000`。

### 查询某日运动记录

```http
GET /api/exercise-records?date=2026-06-08
```

不传 `date` 时默认查询今天。

### 删除运动记录

```http
DELETE /api/exercise-records/{id}
```

## Weight Records

### 新增体重记录

```http
POST /api/weight-records
Content-Type: application/json
```

请求体：

```json
{
  "recordDate": "2026-06-08",
  "weightKg": 74.2,
  "bodyFatPercentage": 23.4,
  "note": "optional"
}
```

`bodyFatPercentage` 和 `note` 可为空。
数值范围：`weightKg` 为 `1.0` 到 `500.0`，`bodyFatPercentage` 为 `0.0` 到 `100.0`。

### 查询近期体重记录

```http
GET /api/weight-records/recent?days=30
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
    "createdAt": "2026-06-08T10:00:00",
    "updatedAt": "2026-06-08T10:00:00"
  }
]
```

### 删除体重记录

```http
DELETE /api/weight-records/{id}
```

## Reports

### 周期报表

```http
GET /api/reports/overview?days=7
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
GET /api/summaries/daily?date=2026-06-08
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

`goalStatus` 可选值：`UNDER`、`MEET`、`OVER`。

### 最近趋势

```http
GET /api/summaries/recent?days=7
```

`days` 范围是 `1` 到 `90`，默认 `7`。
