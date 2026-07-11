# 减肥追踪 AstrBot 插件

## 插件简介

该插件把 AstrBot 聊天转换为 Weight Loss Tracker V2 的结构化记录。AstrBot 负责理解用户意图和生成工具参数，Spring Boot 后端负责数据校验、QQ 用户隔离、持久化、汇总和报表。

插件不直接读写 H2，也不保存独立 LLM API Key。

## 当前功能

- 以 `aiocqhttp` 发送者 QQ 号自动建档
- 查询和更新个人目标资料
- 记录食物、运动和体重
- 查询每日汇总、7 天趋势和周期报表
- LLM 估算结果预览、确认和取消
- 撤销当前插件会话中的最近一条写入
- `/减重状态`、`/减重确认`、`/减重取消`、`/体重` 备用指令

插件注册的 LLM tools：`weight_profile_get`、`weight_profile_update`、`weight_food_record`、`weight_exercise_record`、`weight_body_record`、`weight_daily_summary`、`weight_period_report`、`weight_confirm`、`weight_cancel`、`weight_undo_last`。

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

用户明确提供热量或体重数值时可以直接写入。LLM 推断的食物营养和运动消耗必须先展示预览，用户确认后才写入。所有估算仅供日常记录参考，不构成医疗建议。

## TODO

- 多食物拆分
- 待确认操作持久化
- 图片食物识别
- 营养数据库校准
- 健康设备和 App 数据同步

## 更新历史

### v0.1.0

- 首个 QQ 多用户 MVP
- 增加记录、查询、确认、取消和撤销工具
- 接入本地 V2 HTTP API
