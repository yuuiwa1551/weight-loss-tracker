from unittest import IsolatedAsyncioTestCase

from core.api_client import ApiError
from core.identity import RequestContext, UserIdentity
from core.pending import PendingStore
from core.service import WeightLossService


BUDGET = {
    "todayIntakeBudgetCalories": 2100,
    "caloriesConsumed": 500,
    "remainingIntakeCalories": 1600,
    "estimatedTotalExpenditureCalories": 2400,
    "projectedDeficitCalories": 300,
}


class FakeApi:
    def __init__(self):
        self.created = []
        self.deleted = []
        self.previews = []
        self.confirmed_plans = []
        self.fail_food_once = False
        self.records_by_request_id = {}
        self.profile = {
            "nickname": "雪",
            "heightCm": 170,
            "currentWeightKg": 70,
            "targetWeightKg": 60,
            "dailyCalorieGoal": None,
            "ageYears": None,
            "formulaSex": None,
            "nonExerciseActivityLevel": None,
            "calorieGoalMode": "UNSET",
        }

    async def resolve_user(self, platform, username, display_name):
        return {"id": int(username[-3:])}

    async def get_profile(self, user_id):
        return self.profile.copy()

    async def update_profile(self, user_id, payload):
        self.profile.update(payload)
        return self.profile.copy()

    async def preview_food(self, user_id, payload):
        self.previews.append(("food", user_id, payload.copy()))
        return {
            **payload,
            "previewFingerprint": "food-fingerprint",
            "projectedEnergyBudget": BUDGET,
        }

    async def create_food(self, user_id, payload):
        if self.fail_food_once:
            self.fail_food_once = False
            raise ApiError("模拟后端故障")
        return self._create("food", user_id, payload)

    async def preview_exercise(self, user_id, payload):
        self.previews.append(("exercise", user_id, payload.copy()))
        return {
            **payload,
            "previewFingerprint": "exercise-fingerprint",
            "projectedEnergyBudget": BUDGET,
        }

    async def create_exercise(self, user_id, payload):
        return self._create("exercise", user_id, payload)

    async def preview_energy_plan(self, user_id, payload):
        self.previews.append(("energy_plan", user_id, payload.copy()))
        return {
            "calculation": {
                "restingEnergyCalories": 1700,
                "baselineExpenditureCalories": 2300,
                "dailyDeficitCalories": payload.get("dailyDeficitCalories") or 350,
                "baseIntakeTargetCalories": 1950,
            },
            "previewFingerprint": "plan-fingerprint",
        }

    async def confirm_energy_plan(self, user_id, payload):
        self.confirmed_plans.append((user_id, payload))
        return {
            "id": 1,
            "calculation": {
                "restingEnergyCalories": 1700,
                "baselineExpenditureCalories": 2300,
                "dailyDeficitCalories": payload["calculation"].get("dailyDeficitCalories") or 350,
                "baseIntakeTargetCalories": 1950,
            },
        }

    async def daily_energy_budget(self, user_id, date=""):
        return BUDGET

    async def create_weight(self, user_id, payload):
        return self._create("weight", user_id, payload)

    def _create(self, kind, user_id, payload):
        request_id = payload["clientRequestId"]
        if request_id in self.records_by_request_id:
            return self.records_by_request_id[request_id]
        record = {"id": len(self.created) + 1, **payload, "energyBudget": BUDGET}
        self.records_by_request_id[request_id] = record
        self.created.append((kind, user_id, payload.copy()))
        return record

    async def delete_record(self, user_id, kind, record_id):
        self.deleted.append((user_id, kind, record_id))


def make_context(user="1154824108", message_id="message-1", raw_message="午饭 500 千卡"):
    identity = UserIdentity("aiocqhttp", user, "雪")
    return RequestContext(identity, message_id, raw_message)


class ServiceTests(IsolatedAsyncioTestCase):
    async def asyncSetUp(self):
        self.api = FakeApi()
        self.service = WeightLossService(self.api, PendingStore(600))

    async def test_profile_update_forwards_energy_inputs(self):
        result = await self.service.update_profile(
            make_context(),
            age_years=24,
            formula_sex="female",
            non_exercise_activity_level="moderate",
            calorie_goal_mode="auto",
        )
        self.assertIn("资料已更新", result)
        self.assertEqual(24, self.api.profile["ageYears"])
        self.assertEqual("FEMALE", self.api.profile["formulaSex"])
        self.assertEqual("MODERATE", self.api.profile["nonExerciseActivityLevel"])
        self.assertEqual("AUTO", self.api.profile["calorieGoalMode"])

    async def test_estimated_food_previews_and_writes_in_one_call(self):
        context = make_context(raw_message="午饭吃了一份鸡肉沙拉")
        result = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 460, 38, 18, 28
        )
        self.assertIn("饮食已记录", result)
        self.assertIn("剩余 1600 kcal", result)
        self.assertIsNone(self.service.pending.get(context.identity.key))
        self.assertEqual("food", self.api.created[0][0])
        self.assertEqual("food-fingerprint", self.api.created[0][2]["previewFingerprint"])
        self.assertEqual("LLM_ESTIMATE", self.api.created[0][2]["nutritionSource"])
        self.assertIn("没有等待确认", await self.service.confirm(context))

    async def test_user_provided_food_writes_immediately_and_is_idempotent(self):
        context = make_context(raw_message="午饭鸡肉沙拉 500 千卡")
        first = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 500, is_estimate=False
        )
        second = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 500, is_estimate=False
        )
        self.assertIn("饮食已记录", first)
        self.assertIn("饮食已记录", second)
        self.assertEqual(1, len(self.api.created))
        self.assertEqual("USER_PROVIDED", self.api.created[0][2]["nutritionSource"])
        self.assertIsNone(self.service.pending.get(context.identity.key))

    async def test_backend_failure_leaves_no_pending_and_can_be_retried(self):
        context = make_context(raw_message="吃了一份估算 400 卡的饭")
        self.api.fail_food_once = True
        with self.assertRaises(ApiError):
            await self.service.record_food(
                context, "2026-07-12", "DINNER", "盖饭", 400, is_estimate=True
            )
        self.assertIsNone(self.service.pending.get(context.identity.key))
        self.assertEqual([], self.api.created)

        result = await self.service.record_food(
            context, "2026-07-12", "DINNER", "盖饭", 400, is_estimate=True
        )
        self.assertIn("饮食已记录", result)
        self.assertEqual(1, len(self.api.created))

    async def test_energy_plan_confirmation_remains_per_qq(self):
        first = make_context(user="10001", raw_message="估算的饭")
        second = make_context(user="10002", raw_message="估算的饭")
        await self.service.preview_energy_plan(first, daily_deficit_calories=300)
        await self.service.preview_energy_plan(second, daily_deficit_calories=400)

        await self.service.confirm(first)
        self.assertEqual(1, len(self.api.confirmed_plans))
        self.assertIsNotNone(self.service.pending.get(second.identity.key))

    async def test_user_provided_exercise_previews_and_writes_in_one_call(self):
        context = make_context(raw_message="跑步 30 分钟消耗 260 千卡")
        result = await self.service.record_exercise(
            context, "2026-07-12", "CARDIO", "跑步", 30, 260, is_estimate=False
        )
        self.assertIn("运动已记录", result)
        self.assertIn("剩余 1600 kcal", result)
        self.assertEqual("exercise", self.api.created[0][0])
        self.assertEqual("exercise-fingerprint", self.api.created[0][2]["previewFingerprint"])
        self.assertIsNone(self.service.pending.get(context.identity.key))

    async def test_energy_plan_requires_confirmation(self):
        context = make_context(raw_message="每天留 400 千卡缺口")
        preview = await self.service.preview_energy_plan(context, daily_deficit_calories=400)
        self.assertIn("静息消耗 1700 kcal", preview)
        self.assertIn("每日缺口 400 kcal", preview)
        self.assertEqual([], self.api.confirmed_plans)

        confirmed = await self.service.confirm(context)
        self.assertIn("热量计划已确认", confirmed)
        self.assertEqual("plan-fingerprint", self.api.confirmed_plans[0][1]["previewFingerprint"])

    async def test_automatic_food_can_be_undone_with_confirmation(self):
        context = make_context(raw_message="午饭鸡肉沙拉 500 千卡")
        await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 500, is_estimate=False
        )
        preview = self.service.undo_last(context)
        self.assertIn("待撤销", preview)
        self.assertEqual([], self.api.deleted)

        result = await self.service.confirm(context)
        self.assertIn("已撤销", result)
        self.assertEqual([(108, "food", 1)], self.api.deleted)

    async def test_daily_budget_is_read_only(self):
        result = await self.service.daily_energy_budget(make_context(), "2026-07-12")
        self.assertIn("今日预算 2100 kcal", result)
        self.assertIn("预计缺口 300 kcal", result)
        self.assertEqual([], self.api.created)

    async def test_unset_daily_budget_has_clear_message(self):
        self.api.daily_energy_budget = lambda user_id, date="": _async_value(
            {"goalMode": "UNSET", "todayIntakeBudgetCalories": None}
        )
        result = await self.service.daily_energy_budget(make_context())
        self.assertEqual("当前未启用每日热量预算。", result)

    async def test_weight_is_direct_and_undo_requires_confirmation(self):
        context = make_context(raw_message="今天体重 72.4 kg")
        await self.service.record_weight(context, "2026-07-12", 72.4)
        self.assertEqual([], self.api.deleted)

        preview = self.service.undo_last(context)
        self.assertIn("待撤销", preview)
        self.assertEqual([], self.api.deleted)

        result = await self.service.confirm(context)
        self.assertIn("已撤销", result)
        self.assertEqual([(108, "weight", 1)], self.api.deleted)


async def _async_value(value):
    return value
