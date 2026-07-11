from unittest import TestCase

from core.pending import PendingAction, PendingStore


class PendingStoreTests(TestCase):
    def test_pending_actions_are_isolated_and_expire(self):
        now = [100.0]
        store = PendingStore(ttl_seconds=10, clock=lambda: now[0])
        action = PendingAction("food", 1, {}, "preview", now[0])
        store.put("aiocqhttp:1", action)

        self.assertIs(action, store.get("aiocqhttp:1"))
        self.assertIsNone(store.get("aiocqhttp:2"))

        now[0] = 111.0
        self.assertIsNone(store.get("aiocqhttp:1"))
