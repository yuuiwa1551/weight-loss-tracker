from __future__ import annotations

from dataclasses import dataclass
from typing import Any


class IdentityError(ValueError):
    """The event cannot be mapped to a supported backend user."""


@dataclass(frozen=True)
class UserIdentity:
    platform: str
    username: str
    display_name: str

    @property
    def key(self) -> str:
        return f"{self.platform}:{self.username}"


@dataclass(frozen=True)
class RequestContext:
    identity: UserIdentity
    message_id: str
    raw_message: str


def context_from_event(event: Any, allow_private: bool = True, allow_group: bool = True) -> RequestContext:
    platform = str(event.get_platform_name() or "").strip().lower()
    if platform != "aiocqhttp":
        raise IdentityError("当前仅支持 aiocqhttp QQ 平台")

    message_type = event.get_message_type()
    type_value = str(getattr(message_type, "value", message_type)).lower()
    if "group" in type_value and not allow_group:
        raise IdentityError("插件已关闭群聊使用")
    if any(marker in type_value for marker in ("friend", "private")) and not allow_private:
        raise IdentityError("插件已关闭私聊使用")

    username = str(event.get_sender_id() or "").strip()
    if not username.isdigit():
        raise IdentityError("无法从消息事件取得有效 QQ 号")

    display_name = str(event.get_sender_name() or username).strip() or username
    message_obj = getattr(event, "message_obj", None)
    message_id = str(getattr(message_obj, "message_id", "") or "").strip()
    if not message_id:
        message_id = str(getattr(event, "unified_msg_origin", "") or username)
    raw_message = str(getattr(event, "message_str", "") or "")
    return RequestContext(UserIdentity(platform, username, display_name), message_id, raw_message)
