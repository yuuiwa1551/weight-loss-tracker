from __future__ import annotations

import inspect
from datetime import date
from typing import Any, Callable

from astrbot.api import AstrBotConfig, logger
from astrbot.api.event import AstrMessageEvent, filter
from astrbot.api.star import Context, Star, register

from .core import (
    ApiError,
    IdentityError,
    PendingStore,
    WeightLossApiClient,
    WeightLossService,
    context_from_event,
)


@register(
    "astrbot_plugin_weight_loss_tracker",
    "yuuiwa1551",
    "通过聊天记录饮食、运动、体重和减重目标",
    "0.1.0",
    "https://github.com/yuuiwa1551/weight-loss-tracker",
)
class WeightLossTrackerPlugin(Star):
    def __init__(self, context: Context, config: AstrBotConfig) -> None:
        super().__init__(context)
        self.config = config
        self.allow_private = bool(config.get("allow_private", True))
        self.allow_group = bool(config.get("allow_group", True))
        self.api = WeightLossApiClient(
            str(config.get("backend_base_url", "http://weight-loss-tracker:8080")),
            int(config.get("request_timeout_seconds", 5)),
        )
        self.service = WeightLossService(
            self.api,
            PendingStore(int(config.get("pending_ttl_seconds", 600))),
        )
        logger.info(f"[减肥追踪] 插件已加载，后端地址：{self.api.base_url}")

    def _context(self, event: AstrMessageEvent):
        return context_from_event(event, self.allow_private, self.allow_group)

    async def _call(self, event: AstrMessageEvent, operation: Callable[..., Any], *args: Any, **kwargs: Any) -> str:
        try:
            result = operation(self._context(event), *args, **kwargs)
            if inspect.isawaitable(result):
                result = await result
            return str(result)
        except (IdentityError, ApiError) as exc:
            return str(exc)
        except Exception as exc:
            logger.exception("[减肥追踪] 操作失败")
            return f"减肥追踪操作失败：{exc}"

    @filter.llm_tool(name="weight_profile_get")
    async def weight_profile_get(self, event: AstrMessageEvent):
        """查询当前 QQ 用户的减重资料和资料完整度。"""
        return await self._call(event, self.service.get_profile)

    @filter.llm_tool(name="weight_profile_update")
    async def weight_profile_update(
        self,
        event: AstrMessageEvent,
        nickname: str = "",
        height_cm: float | None = None,
        current_weight_kg: float | None = None,
        target_weight_kg: float | None = None,
        daily_calorie_goal: int | None = None,
    ):
        """更新当前 QQ 用户明确提供的减重资料，只传需要更新的字段。

        Args:
            nickname(string): 昵称，可留空
            height_cm(number): 身高厘米，可留空
            current_weight_kg(number): 当前体重公斤，可留空
            target_weight_kg(number): 目标体重公斤，可留空
            daily_calorie_goal(number): 每日热量目标 kcal，可留空
        """
        return await self._call(
            event,
            self.service.update_profile,
            nickname,
            height_cm,
            current_weight_kg,
            target_weight_kg,
            daily_calorie_goal,
        )

    @filter.llm_tool(name="weight_food_record")
    async def weight_food_record(
        self,
        event: AstrMessageEvent,
        record_date: str,
        meal_type: str,
        food_name: str,
        calories: int,
        protein: float = 0,
        fat: float = 0,
        carbohydrate: float = 0,
        note: str = "",
        is_estimate: bool = True,
        estimation_note: str = "",
    ):
        """记录食物。任何由模型推断的热量或营养必须将 is_estimate 设为 true。

        Args:
            record_date(string): ISO 日期 YYYY-MM-DD
            meal_type(string): BREAKFAST、LUNCH、DINNER 或 SNACK
            food_name(string): 食物名称
            calories(number): 热量 kcal
            protein(number): 蛋白质克数
            fat(number): 脂肪克数
            carbohydrate(number): 碳水克数
            note(string): 用户备注
            is_estimate(boolean): 只要任一营养值由模型估算就必须为 true
            estimation_note(string): 估算依据或份量说明
        """
        return await self._call(
            event,
            self.service.record_food,
            record_date,
            meal_type,
            food_name,
            calories,
            protein,
            fat,
            carbohydrate,
            note,
            is_estimate,
            estimation_note,
        )

    @filter.llm_tool(name="weight_exercise_record")
    async def weight_exercise_record(
        self,
        event: AstrMessageEvent,
        record_date: str,
        exercise_type: str,
        exercise_name: str,
        duration_minutes: int,
        calories_burned: int,
        note: str = "",
        is_estimate: bool = True,
    ):
        """记录运动。模型推断消耗热量时必须将 is_estimate 设为 true。

        Args:
            record_date(string): ISO 日期 YYYY-MM-DD
            exercise_type(string): 运动类别
            exercise_name(string): 运动名称
            duration_minutes(number): 时长分钟
            calories_burned(number): 消耗热量 kcal
            note(string): 用户备注
            is_estimate(boolean): 消耗热量由模型估算时为 true
        """
        return await self._call(
            event,
            self.service.record_exercise,
            record_date,
            exercise_type,
            exercise_name,
            duration_minutes,
            calories_burned,
            note,
            is_estimate,
        )

    @filter.llm_tool(name="weight_body_record")
    async def weight_body_record(
        self,
        event: AstrMessageEvent,
        record_date: str,
        weight_kg: float,
        body_fat_percentage: float | None = None,
        note: str = "",
    ):
        """直接记录用户明确上报的体重和可选体脂。

        Args:
            record_date(string): ISO 日期 YYYY-MM-DD
            weight_kg(number): 体重公斤
            body_fat_percentage(number): 体脂百分比，可留空
            note(string): 用户备注
        """
        return await self._call(
            event,
            self.service.record_weight,
            record_date,
            weight_kg,
            body_fat_percentage,
            note,
        )

    @filter.llm_tool(name="weight_daily_summary")
    async def weight_daily_summary(self, event: AstrMessageEvent, record_date: str = ""):
        """查询某日饮食、运动、净热量和目标状态。

        Args:
            record_date(string): ISO 日期 YYYY-MM-DD，留空表示今天
        """
        return await self._call(event, self.service.daily_summary, record_date)

    @filter.llm_tool(name="weight_period_report")
    async def weight_period_report(self, event: AstrMessageEvent, days: int = 7):
        """查询最近 7 到 365 天的热量、营养和体重趋势报表。

        Args:
            days(number): 报表天数，范围 7 到 365
        """
        return await self._call(event, self.service.period_report, days)

    @filter.llm_tool(name="weight_confirm")
    async def weight_confirm(self, event: AstrMessageEvent):
        """确认当前 QQ 用户最近一次待确认的估算记录或撤销操作。"""
        return await self._call(event, self.service.confirm)

    @filter.llm_tool(name="weight_cancel")
    async def weight_cancel(self, event: AstrMessageEvent):
        """取消当前 QQ 用户最近一次待确认操作。"""
        return await self._call(event, self.service.cancel)

    @filter.llm_tool(name="weight_undo_last")
    async def weight_undo_last(self, event: AstrMessageEvent):
        """请求撤销当前插件进程中该 QQ 用户最近一次成功写入，仍需再次确认。"""
        return await self._call(event, self.service.undo_last)

    @filter.command("减重状态")
    async def command_status(self, event: AstrMessageEvent):
        yield event.plain_result(await self._call(event, self.service.daily_summary, ""))

    @filter.command("减重确认")
    async def command_confirm(self, event: AstrMessageEvent):
        yield event.plain_result(await self._call(event, self.service.confirm))

    @filter.command("减重取消")
    async def command_cancel(self, event: AstrMessageEvent):
        yield event.plain_result(await self._call(event, self.service.cancel))

    @filter.command("体重")
    async def command_weight(self, event: AstrMessageEvent, weight_kg: float):
        result = await self._call(
            event,
            self.service.record_weight,
            date.today().isoformat(),
            weight_kg,
            None,
            "AstrBot 指令记录",
        )
        yield event.plain_result(result)

    async def terminate(self) -> None:
        await self.api.close()
        logger.info("[减肥追踪] HTTP 客户端已关闭")
