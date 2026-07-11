from __future__ import annotations

import hashlib
import json
import re
import time
from dataclasses import dataclass
from typing import Any

from .api_client import WeightLossApiClient
from .identity import RequestContext
from .pending import PendingAction, PendingStore


CALORIE_PATTERN = re.compile(r"\d+(?:\.\d+)?\s*(?:kcal|千卡|大卡|卡路里)", re.IGNORECASE)


@dataclass(frozen=True)
class LastWrite:
    kind: str
    user_id: int
    record_id: int
    description: str


class WeightLossService:
    def __init__(self, api: WeightLossApiClient, pending: PendingStore) -> None:
        self.api = api
        self.pending = pending
        self._last_writes: dict[str, LastWrite] = {}

    async def _user_id(self, context: RequestContext) -> int:
        user = await self.api.resolve_user(
            context.identity.platform,
            context.identity.username,
            context.identity.display_name,
        )
        return int(user["id"])

    @staticmethod
    def _request_id(context: RequestContext, kind: str) -> str:
        source = f"{context.identity.key}:{context.message_id}:{kind}"
        digest = hashlib.sha256(source.encode("utf-8")).hexdigest()[:24]
        return f"astrbot:{context.identity.username}:{kind}:{digest}"

    @staticmethod
    def _json(data: Any) -> str:
        return json.dumps(data, ensure_ascii=False, separators=(",", ":"), default=str)

    def _remember(self, context: RequestContext, kind: str, user_id: int, record: dict[str, Any], description: str) -> None:
        self._last_writes[context.identity.key] = LastWrite(
            kind=kind,
            user_id=user_id,
            record_id=int(record["id"]),
            description=description,
        )

    async def get_profile(self, context: RequestContext) -> str:
        profile = await self.api.get_profile(await self._user_id(context))
        return f"当前资料：{self._json(profile)}"

    async def update_profile(
        self,
        context: RequestContext,
        nickname: str = "",
        height_cm: float | None = None,
        current_weight_kg: float | None = None,
        target_weight_kg: float | None = None,
        daily_calorie_goal: int | None = None,
    ) -> str:
        user_id = await self._user_id(context)
        current = await self.api.get_profile(user_id)
        payload = {
            "nickname": nickname.strip() or current.get("nickname"),
            "heightCm": height_cm if height_cm is not None else current.get("heightCm"),
            "currentWeightKg": (
                current_weight_kg if current_weight_kg is not None else current.get("currentWeightKg")
            ),
            "targetWeightKg": (
                target_weight_kg if target_weight_kg is not None else current.get("targetWeightKg")
            ),
            "dailyCalorieGoal": (
                daily_calorie_goal
                if daily_calorie_goal is not None
                else current.get("dailyCalorieGoal")
            ),
        }
        updated = await self.api.update_profile(user_id, payload)
        return f"资料已更新：{self._json(updated)}"

    async def record_food(
        self,
        context: RequestContext,
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
    ) -> str:
        user_id = await self._user_id(context)
        estimated = is_estimate or not CALORIE_PATTERN.search(context.raw_message)
        payload = {
            "recordDate": record_date,
            "mealType": meal_type.upper(),
            "foodName": food_name,
            "calories": calories,
            "protein": protein,
            "fat": fat,
            "carbohydrate": carbohydrate,
            "note": note or None,
            "source": "ASTRBOT",
            "clientRequestId": self._request_id(context, "food"),
            "nutritionSource": "LLM_ESTIMATE" if estimated else "USER_PROVIDED",
            "estimationNote": estimation_note or ("由 LLM 估算" if estimated else None),
        }
        preview = (
            f"待确认饮食：{record_date} {meal_type.upper()}，{food_name}，{calories} kcal，"
            f"蛋白质 {protein}g / 脂肪 {fat}g / 碳水 {carbohydrate}g。"
        )
        if estimated:
            self.pending.put(
                context.identity.key,
                PendingAction("food", user_id, payload, preview, time.monotonic()),
            )
            return preview + "这是估算值，请回复确认或调用 weight_confirm；取消请调用 weight_cancel。"
        record = await self.api.create_food(user_id, payload)
        self._remember(context, "food", user_id, record, food_name)
        return f"饮食已记录：{food_name}，{calories} kcal。"

    async def record_exercise(
        self,
        context: RequestContext,
        record_date: str,
        exercise_type: str,
        exercise_name: str,
        duration_minutes: int,
        calories_burned: int,
        note: str = "",
        is_estimate: bool = True,
    ) -> str:
        user_id = await self._user_id(context)
        estimated = is_estimate or not CALORIE_PATTERN.search(context.raw_message)
        payload = {
            "recordDate": record_date,
            "exerciseType": exercise_type,
            "exerciseName": exercise_name,
            "durationMinutes": duration_minutes,
            "caloriesBurned": calories_burned,
            "note": note or None,
            "source": "ASTRBOT",
            "clientRequestId": self._request_id(context, "exercise"),
        }
        preview = (
            f"待确认运动：{record_date} {exercise_name}，{duration_minutes} 分钟，"
            f"消耗 {calories_burned} kcal。"
        )
        if estimated:
            self.pending.put(
                context.identity.key,
                PendingAction("exercise", user_id, payload, preview, time.monotonic()),
            )
            return preview + "这是估算值，请回复确认或调用 weight_confirm；取消请调用 weight_cancel。"
        record = await self.api.create_exercise(user_id, payload)
        self._remember(context, "exercise", user_id, record, exercise_name)
        return f"运动已记录：{exercise_name}，消耗 {calories_burned} kcal。"

    async def record_weight(
        self,
        context: RequestContext,
        record_date: str,
        weight_kg: float,
        body_fat_percentage: float | None = None,
        note: str = "",
    ) -> str:
        user_id = await self._user_id(context)
        payload = {
            "recordDate": record_date,
            "weightKg": weight_kg,
            "bodyFatPercentage": body_fat_percentage,
            "note": note or None,
            "source": "ASTRBOT",
            "clientRequestId": self._request_id(context, "weight"),
        }
        record = await self.api.create_weight(user_id, payload)
        self._remember(context, "weight", user_id, record, f"体重 {weight_kg} kg")
        return f"体重已记录：{weight_kg} kg。"

    async def confirm(self, context: RequestContext) -> str:
        action = self.pending.get(context.identity.key)
        if action is None:
            return "没有等待确认的记录，或待确认记录已过期。"
        if action.kind == "food":
            record = await self.api.create_food(action.user_id, action.payload)
            description = str(action.payload["foodName"])
        elif action.kind == "exercise":
            record = await self.api.create_exercise(action.user_id, action.payload)
            description = str(action.payload["exerciseName"])
        elif action.kind == "delete":
            await self.api.delete_record(
                action.user_id,
                str(action.payload["recordKind"]),
                int(action.payload["recordId"]),
            )
            self.pending.pop(context.identity.key)
            self._last_writes.pop(context.identity.key, None)
            return "最近一条记录已撤销。"
        else:
            return f"不支持的待确认操作：{action.kind}"

        self.pending.pop(context.identity.key)
        self._remember(context, action.kind, action.user_id, record, description)
        return f"已确认并写入：{description}。"

    def cancel(self, context: RequestContext) -> str:
        if self.pending.cancel(context.identity.key):
            return "待确认操作已取消。"
        return "没有等待取消的操作。"

    def undo_last(self, context: RequestContext) -> str:
        last = self._last_writes.get(context.identity.key)
        if last is None:
            return "当前插件会话中没有可撤销的写入。"
        preview = f"待撤销：{last.description}（{last.kind} #{last.record_id}）。"
        self.pending.put(
            context.identity.key,
            PendingAction(
                "delete",
                last.user_id,
                {"recordKind": last.kind, "recordId": last.record_id},
                preview,
                time.monotonic(),
            ),
        )
        return preview + "请回复确认或调用 weight_confirm。"

    async def daily_summary(self, context: RequestContext, date: str = "") -> str:
        summary = await self.api.daily_summary(await self._user_id(context), date)
        return f"每日汇总：{self._json(summary)}"

    async def recent_summary(self, context: RequestContext, days: int = 7) -> str:
        summary = await self.api.recent_summary(await self._user_id(context), days)
        return f"近期趋势：{self._json(summary)}"

    async def period_report(self, context: RequestContext, days: int = 7) -> str:
        report = await self.api.period_report(await self._user_id(context), days)
        return f"周期报表：{self._json(report)}"
