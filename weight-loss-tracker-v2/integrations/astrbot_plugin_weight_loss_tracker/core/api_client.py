from __future__ import annotations

import asyncio
from typing import Any

import aiohttp


class ApiError(RuntimeError):
    """A backend transport or application error safe to show to the user."""


class WeightLossApiClient:
    def __init__(self, base_url: str, timeout_seconds: int = 5) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = aiohttp.ClientTimeout(total=max(1, timeout_seconds))
        self._session: aiohttp.ClientSession | None = None

    async def close(self) -> None:
        if self._session and not self._session.closed:
            await self._session.close()

    async def _get_session(self) -> aiohttp.ClientSession:
        if self._session is None or self._session.closed:
            self._session = aiohttp.ClientSession(timeout=self.timeout)
        return self._session

    async def _request(
        self,
        method: str,
        path: str,
        *,
        json: dict[str, Any] | None = None,
        params: dict[str, Any] | None = None,
    ) -> Any:
        url = f"{self.base_url}{path}"
        for attempt in range(2):
            try:
                session = await self._get_session()
                async with session.request(method, url, json=json, params=params) as response:
                    try:
                        payload = await response.json(content_type=None)
                    except (aiohttp.ContentTypeError, ValueError) as exc:
                        raise ApiError(f"后端返回了无法解析的响应（HTTP {response.status}）") from exc

                    if response.status >= 400 or not payload.get("success", False):
                        message = payload.get("message") or f"HTTP {response.status}"
                        raise ApiError(f"后端请求失败：{message}")
                    return payload.get("data")
            except (aiohttp.ClientConnectionError, asyncio.TimeoutError) as exc:
                if attempt == 0:
                    await asyncio.sleep(0.15)
                    continue
                raise ApiError(f"无法连接减肥追踪后端：{exc}") from exc
        raise ApiError("无法连接减肥追踪后端")

    async def resolve_user(self, platform: str, username: str, display_name: str) -> dict[str, Any]:
        return await self._request(
            "POST",
            "/api/users/resolve",
            json={"platform": platform, "username": username, "displayName": display_name},
        )

    async def get_profile(self, user_id: int) -> dict[str, Any]:
        return await self._request("GET", f"/api/users/{user_id}/profile")

    async def update_profile(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("PUT", f"/api/users/{user_id}/profile", json=payload)

    async def create_food(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", f"/api/users/{user_id}/food-records", json=payload)

    async def preview_food(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request(
            "POST", f"/api/users/{user_id}/food-records/preview", json=payload
        )

    async def create_exercise(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", f"/api/users/{user_id}/exercise-records", json=payload)

    async def preview_exercise(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request(
            "POST", f"/api/users/{user_id}/exercise-records/preview", json=payload
        )

    async def preview_energy_plan(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request(
            "POST", f"/api/users/{user_id}/energy-plans/preview", json=payload
        )

    async def confirm_energy_plan(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", f"/api/users/{user_id}/energy-plans", json=payload)

    async def daily_energy_budget(self, user_id: int, date: str = "") -> dict[str, Any]:
        params = {"date": date} if date else None
        return await self._request(
            "GET", f"/api/users/{user_id}/energy-budgets/daily", params=params
        )

    async def create_weight(self, user_id: int, payload: dict[str, Any]) -> dict[str, Any]:
        return await self._request("POST", f"/api/users/{user_id}/weight-records", json=payload)

    async def delete_record(self, user_id: int, kind: str, record_id: int) -> None:
        paths = {
            "food": "food-records",
            "exercise": "exercise-records",
            "weight": "weight-records",
        }
        resource = paths.get(kind)
        if resource is None:
            raise ApiError(f"不支持删除记录类型：{kind}")
        await self._request("DELETE", f"/api/users/{user_id}/{resource}/{record_id}")

    async def daily_summary(self, user_id: int, date: str = "") -> dict[str, Any]:
        params = {"date": date} if date else None
        return await self._request("GET", f"/api/users/{user_id}/summaries/daily", params=params)

    async def recent_summary(self, user_id: int, days: int = 7) -> dict[str, Any]:
        return await self._request(
            "GET", f"/api/users/{user_id}/summaries/recent", params={"days": days}
        )

    async def period_report(self, user_id: int, days: int = 7) -> dict[str, Any]:
        return await self._request(
            "GET", f"/api/users/{user_id}/reports/overview", params={"days": days}
        )
