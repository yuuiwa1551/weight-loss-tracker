# 能量计算契约 P6_V1

状态：Phase 6 第 1 阶段冻结版本。实现必须通过新版本号显式变更本文件中的口径，不能静默调整历史计划。

## 1. 输入

- 年龄：`ageYears`
- 计算用性别：`formulaSex`
- 身高：`heightCm`
- 最新体重：`weightKg`
- 非运动日常活动等级：`nonExerciseActivityLevel`
- 目标体重：`targetWeightKg`，仅目标周期模式需要
- 每日缺口或目标周期：两者最多提供一个

专项运动不得进入日常活动等级。跑步、力量训练、舞蹈等只通过实际日期上的运动记录计入。

## 2. 静息消耗

方法标识：`MIFFLIN_ST_JEOR`

版本标识：`P6_V1`

```text
基础项 = 10 × weightKg + 6.25 × heightCm - 5 × ageYears

MALE   restingEnergy = 基础项 + 5
FEMALE restingEnergy = 基础项 - 161
```

中间值使用 `BigDecimal`，最终 kcal 使用 `HALF_UP` 舍入为整数。

## 3. 非运动日常活动系数

这些系数是本项目 P6_V1 的产品估算口径，不表示医学结论：

| 等级 | 系数 |
| --- | ---: |
| `SEDENTARY` | 1.20 |
| `LIGHT` | 1.30 |
| `MODERATE` | 1.45 |
| `HIGH` | 1.60 |

```text
baselineExpenditure = restingEnergy × activityFactor
```

## 4. 每日缺口

按以下优先级选择一种模式：

1. `EXPLICIT`：使用用户明确提供并确认的 `dailyDeficitCalories`
2. `TARGET_PERIOD`：`(currentWeightKg - targetWeightKg) × 7700 ÷ targetPeriodDays`
3. `DEFAULT_RATE`：`baselineExpenditure × 0.15`

P6 不对计算后的缺口、目标体重或摄入预算设置医学安全上下限。结果仍必须是有限数值，并统一 `HALF_UP` 舍入为整数 kcal。

## 5. 基础与当日预算

```text
baseIntakeTarget = baselineExpenditure - dailyDeficit

exerciseCalories = 当日已确认运动记录 caloriesBurned 之和

estimatedTotalExpenditure = baselineExpenditure + exerciseCalories

todayIntakeBudget = baseIntakeTarget + exerciseCalories

remainingIntake = todayIntakeBudget - confirmedCaloriesConsumed

projectedDeficit = estimatedTotalExpenditure - confirmedCaloriesConsumed
```

剩余预算和预计缺口允许为负数。未确认食物和运动不得参与任何汇总。

## 6. 预览与确认

计划、食物和运动都执行相同流程：

1. 后端根据当前权威状态计算预览，不写业务表
2. 响应返回全部待确认数值、预计预算变化和 `previewFingerprint`
3. 用户明确确认
4. 客户端携带原始输入、`clientRequestId` 和指纹发起正式创建
5. 后端重新计算并核对指纹
6. 指纹一致时原子写入；不一致返回 `409 Conflict`

用户本人明确提供的数值也不能绕过预览。

## 7. 指纹输入

`previewFingerprint` 使用 canonical JSON 的 SHA-256 小写十六进制摘要，canonical JSON 至少包含：

- `contractVersion=P6_V1`
- `userId`
- 操作类型
- 用户资料 `updatedAt`
- 当前活动计划 ID 和 `updatedAt`
- 记录日期
- 原始请求参数
- 规范化后的待确认参数
- 所有计算结果
- 该日期已确认食物和运动记录的有序状态摘要

数组按稳定业务键排序，对象键按字典序输出，数值使用无指数十进制形式。指纹不是鉴权凭据，只用于确认用户看到的数值仍与写入时状态一致。

## 8. 兼容

- Phase 6 接口启用前，现有 P5 API 行为保持不变
- Phase 6 正式启用时，食物和运动创建接口开始强制要求有效指纹
- 现有手动 `dailyCalorieGoal` 继续工作
- 历史计划保留其 `calculationMethod` 和 `calculationVersion`
