export function today() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatShortDate(value: string) {
  return value.slice(5).replace('-', '/')
}

export function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

export function selectInitialUserId(users: AppUser[], storedValue: string | null) {
  const storedId = storedValue === null ? Number.NaN : Number(storedValue)
  return users.some((user) => user.id === storedId) ? storedId : (users[0]?.id ?? null)
}
import type { AppUser } from './api'
