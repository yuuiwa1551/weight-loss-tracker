# 减肥追踪 AstrBot 插件

## 插件简介

该插件把 AstrBot 聊天转换为 Weight Loss Tracker V2 的结构化记录。AstrBot 负责理解用户意图和生成工具参数，Spring Boot 后端负责数据校验、QQ 用户隔离、持久化、汇总和报表。

插件不直接读写 H2，也不保存独立 LLM API Key。

## 当前功能

- 以 `aiocqhttp` 发送者 QQ 号自动建档
- 查询和更新个人目标资料，包括年龄、公式性别、活动量和热量目标模式
- 预览并确认静息消耗、日常总消耗、每日缺口和基础摄入预算
- 食物与运动的所有数值都由后端预览，确认后才写入；体重保持直接上报
- 查询每日动态摄入预算、汇总、7 天趋势和周期报表
- 统一确认、取消和过期处理
- 撤销当前插件会话中的最近一条写入
- `/减重状态`、`/减重确认`、`/减重取消`、`/体重` 备用指令

插件注册的 LLM tools：`weight_profile_get`、`weight_profile_update`、`weight_energy_plan_preview`、`weight_energy_budget_get`、`weight_food_record`、`weight_exercise_record`、`weight_body_record`、`weight_daily_summary`、`weight_period_report`、`weight_confirm`、`weight_cancel`、`weight_undo_last`。

## 配置

- `backend_base_url`：后端地址，默认 `http://weight-loss-tracker:8080`
- `allow_private`：允许私聊，默认开启
- `allow_group`：允许群聊，默认开启
- `pending_ttl_seconds`：待确认有效期，默认 600 秒
- `request_timeout_seconds`：HTTP 超时，默认 5 秒

## WebUI

插件没有独立 WebUI，也不挂载 AstrBot Dashboard 页面。数据管理使用 Weight Loss Tracker 自带的本地 Web 页面。

## 数据位置

- 业务数据：Weight Loss Tracker 后端挂载的 H2 数据目录
- 插件待确认项：仅保存在 AstrBot 进程内存，重启后清空
- 插件源码：V2 仓库 `integrations/astrbot_plugin_weight_loss_tracker`
- 运行副本：AstrBot `data/plugins/astrbot_plugin_weight_loss_tracker`

## 调用规则

静息消耗、日常总消耗、每日缺口和基础摄入预算作为一份计划统一预览，用户确认后才启用。每次进食和运动的所有数值，无论来自用户、设备还是 LLM，都必须先展示后端生成的预览和预计每日预算，再由用户确认写入。体重上报不属于本次能量数值确认范围，仍直接写入。

每个 QQ 用户同一时间只保留最近一项待确认操作。确认时后端重新校验 preview 指纹；若资料、计划或当日记录已变化，确认会失败，必须重新预览。

## TODO

- 多食物拆分
- 待确认操作持久化
- 图片食物识别
- 营养数据库校准
- 健康设备和 App 数据同步

## 更新历史

### v0.2.0

- 接入 P6 能量资料、计划预览确认和每日动态预算
- 食物与运动统一改为后端预览、QQ 用户确认后写入
- 确认结果返回剩余摄入预算和预计缺口

### v0.1.0

- 首个 QQ 多用户 MVP
- 增加记录、查询、确认、取消和撤销工具
- 接入本地 V2 HTTP API
