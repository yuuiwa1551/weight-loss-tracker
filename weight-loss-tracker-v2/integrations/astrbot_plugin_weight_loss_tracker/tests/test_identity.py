from types import SimpleNamespace
from unittest import TestCase

from core.identity import IdentityError, context_from_event


class FakeEvent:
    def __init__(self, user_id="1154824108", message_type="GroupMessage", platform="aiocqhttp"):
        self.user_id = user_id
        self.message_type = message_type
        self.platform = platform
        self.message_obj = SimpleNamespace(message_id="message-1")
        self.message_str = "测试消息"

    def get_platform_name(self):
        return self.platform

    def get_message_type(self):
        return self.message_type

    def get_sender_id(self):
        return self.user_id

    def get_sender_name(self):
        return "雪"


class IdentityTests(TestCase):
    def test_same_qq_has_same_identity_in_private_and_group(self):
        group = context_from_event(FakeEvent(message_type="GroupMessage"))
        private = context_from_event(FakeEvent(message_type="FriendMessage"))
        self.assertEqual(group.identity.key, private.identity.key)
        self.assertEqual("aiocqhttp:1154824108", group.identity.key)

    def test_rejects_non_qq_platform_and_invalid_sender(self):
        with self.assertRaises(IdentityError):
            context_from_event(FakeEvent(platform="webchat"))
        with self.assertRaises(IdentityError):
            context_from_event(FakeEvent(user_id="not-a-qq"))

    def test_respects_private_and_group_switches(self):
        with self.assertRaises(IdentityError):
            context_from_event(FakeEvent(message_type="GroupMessage"), allow_group=False)
        with self.assertRaises(IdentityError):
            context_from_event(FakeEvent(message_type="FriendMessage"), allow_private=False)
