from unittest import IsolatedAsyncioTestCase

from core.api_client import ApiError
from core.identity import RequestContext, UserIdentity
from core.pending import PendingStore
from core.service import WeightLossService


class FakeApi:
    def __init__(self):
        self.created = []
        self.deleted = []
        self.fail_food_once = False
        self.records_by_request_id = {}

    async def resolve_user(self, platform, username, display_name):
        return {"id": int(username[-3:])}

    async def create_food(self, user_id, payload):
        if self.fail_food_once:
            self.fail_food_once = False
            raise ApiError("模拟后端故障")
        return self._create("food", user_id, payload)

    async def create_exercise(self, user_id, payload):
        return self._create("exercise", user_id, payload)

    async def create_weight(self, user_id, payload):
        return self._create("weight", user_id, payload)

    def _create(self, kind, user_id, payload):
        request_id = payload["clientRequestId"]
        if request_id in self.records_by_request_id:
            return self.records_by_request_id[request_id]
        record = {"id": len(self.created) + 1, **payload}
        self.records_by_request_id[request_id] = record
        self.created.append((kind, user_id, payload))
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

    async def test_estimated_food_requires_confirmation(self):
        context = make_context(raw_message="午饭吃了一份鸡肉沙拉")
        result = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 460, 38, 18, 28
        )
        self.assertIn("待确认", result)
        self.assertEqual([], self.api.created)

        confirmed = await self.service.confirm(context)
        self.assertIn("已确认并写入", confirmed)
        self.assertEqual("food", self.api.created[0][0])
        self.assertEqual("LLM_ESTIMATE", self.api.created[0][2]["nutritionSource"])

    async def test_explicit_user_calories_write_directly_and_are_idempotent(self):
        context = make_context(raw_message="午饭鸡肉沙拉 500 千卡")
        first = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 500, is_estimate=False
        )
        second = await self.service.record_food(
            context, "2026-07-12", "LUNCH", "鸡肉沙拉", 500, is_estimate=False
        )
        self.assertIn("已记录", first)
        self.assertIn("已记录", second)
        self.assertEqual(1, len(self.api.created))
        self.assertEqual("USER_PROVIDED", self.api.created[0][2]["nutritionSource"])

    async def test_backend_failure_keeps_pending_action(self):
        context = make_context(raw_message="吃了一份估算 400 卡的饭")
        await self.service.record_food(
            context, "2026-07-12", "DINNER", "盖饭", 400, is_estimate=True
        )
        self.api.fail_food_once = True
        with self.assertRaises(ApiError):
            await self.service.confirm(context)
        self.assertIsNotNone(self.service.pending.get(context.identity.key))

        result = await self.service.confirm(context)
        self.assertIn("已确认并写入", result)

    async def test_pending_confirmation_is_per_qq(self):
        first = make_context(user="10001", raw_message="估算的饭")
        second = make_context(user="10002", raw_message="估算的饭")
        await self.service.record_food(first, "2026-07-12", "DINNER", "A", 300)
        await self.service.record_food(second, "2026-07-12", "DINNER", "B", 400)

        await self.service.confirm(first)
        self.assertEqual("A", self.api.created[0][2]["foodName"])
        self.assertIsNotNone(self.service.pending.get(second.identity.key))

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
