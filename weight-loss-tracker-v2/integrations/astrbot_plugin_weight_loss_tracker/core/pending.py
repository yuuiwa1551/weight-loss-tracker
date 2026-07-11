from __future__ import annotations

import time
from dataclasses import dataclass
from typing import Any, Callable


@dataclass(frozen=True)
class PendingAction:
    kind: str
    user_id: int
    payload: dict[str, Any]
    preview: str
    created_at: float


class PendingStore:
    def __init__(self, ttl_seconds: int = 600, clock: Callable[[], float] = time.monotonic) -> None:
        self.ttl_seconds = max(1, ttl_seconds)
        self.clock = clock
        self._items: dict[str, PendingAction] = {}

    def put(self, identity_key: str, action: PendingAction) -> None:
        self._items[identity_key] = action

    def get(self, identity_key: str) -> PendingAction | None:
        action = self._items.get(identity_key)
        if action is None:
            return None
        if self.clock() - action.created_at > self.ttl_seconds:
            self._items.pop(identity_key, None)
            return None
        return action

    def pop(self, identity_key: str) -> PendingAction | None:
        action = self.get(identity_key)
        if action is not None:
            self._items.pop(identity_key, None)
        return action

    def cancel(self, identity_key: str) -> bool:
        return self._items.pop(identity_key, None) is not None
