import { beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from './api'

const fetchMock = vi.fn()

function successfulResponse<T>(data: T) {
  return {
    ok: true,
    status: 200,
    json: async () => ({ success: true, message: 'ok', data }),
  } as Response
}

describe('api client', () => {
  beforeEach(() => {
    fetchMock.mockReset()
    vi.stubGlobal('fetch', fetchMock)
  })

  it('adds the selected user id to profile and summary requests', async () => {
    fetchMock
      .mockResolvedValueOnce(successfulResponse({ id: 42 }))
      .mockResolvedValueOnce(successfulResponse({ date: '2026-07-12' }))

    await api.getProfile(42)
    await api.getDailySummary(42, '2026-07-12')

    expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/users/42/profile', expect.any(Object))
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/users/42/summaries/daily?date=2026-07-12',
      expect.any(Object),
    )
  })

  it('keeps create and delete operations inside the selected user scope', async () => {
    fetchMock
      .mockResolvedValueOnce(successfulResponse({ id: 9 }))
      .mockResolvedValueOnce(successfulResponse(null))

    await api.createWeightRecord(7, {
      recordDate: '2026-07-12',
      weightKg: 72.4,
    })
    await api.deleteWeightRecord(7, 9)

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      '/api/users/7/weight-records',
      expect.objectContaining({ method: 'POST' }),
    )
    expect(fetchMock).toHaveBeenNthCalledWith(
      2,
      '/api/users/7/weight-records/9',
      expect.objectContaining({ method: 'DELETE' }),
    )
  })
})
