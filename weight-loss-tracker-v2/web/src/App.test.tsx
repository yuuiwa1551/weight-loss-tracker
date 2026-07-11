import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import type { AppUser, DailySummary, PeriodReport, UserProfile } from './api'

const apiMocks = vi.hoisted(() => ({
  getUsers: vi.fn(),
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
  createFoodRecord: vi.fn(),
  deleteFoodRecord: vi.fn(),
  createExerciseRecord: vi.fn(),
  deleteExerciseRecord: vi.fn(),
  getRecentWeightRecords: vi.fn(),
  createWeightRecord: vi.fn(),
  deleteWeightRecord: vi.fn(),
  getPeriodReport: vi.fn(),
  getDailySummary: vi.fn(),
  getRecentSummaries: vi.fn(),
}))

vi.mock('./api', () => ({ api: apiMocks }))

const users: AppUser[] = [
  { id: 1, platform: 'local', username: 'local', displayName: '本机用户', createdAt: '', updatedAt: '' },
  { id: 2, platform: 'aiocqhttp', username: '1154824108', displayName: '雪', createdAt: '', updatedAt: '' },
]

function profile(userId: number): UserProfile {
  return {
    id: userId,
    nickname: userId === 2 ? '雪' : '本机用户',
    heightCm: null,
    currentWeightKg: null,
    targetWeightKg: null,
    dailyCalorieGoal: null,
    bmi: null,
    weightToLoseKg: null,
    profileComplete: false,
    missingFields: ['heightCm', 'currentWeightKg', 'targetWeightKg', 'dailyCalorieGoal'],
    createdAt: '',
    updatedAt: '',
  }
}

function dailySummary(): DailySummary {
  return {
    date: '2026-07-12',
    totalCaloriesConsumed: 0,
    totalCaloriesBurned: 0,
    netCalories: 0,
    dailyCalorieGoal: null,
    calorieDifference: null,
    goalStatus: 'UNSET',
    totalProtein: 0,
    totalFat: 0,
    totalCarbohydrate: 0,
    foodRecords: [],
    exerciseRecords: [],
  }
}

function report(days: number): PeriodReport {
  return {
    startDate: '2026-07-06',
    endDate: '2026-07-12',
    days,
    totalCaloriesConsumed: 0,
    totalCaloriesBurned: 0,
    netCalories: 0,
    averageCaloriesConsumed: 0,
    averageCaloriesBurned: 0,
    averageNetCalories: 0,
    averageProtein: 0,
    averageFat: 0,
    averageCarbohydrate: 0,
    dailyCalorieGoal: null,
    daysUnderGoal: 0,
    daysMeetGoal: 0,
    daysOverGoal: 0,
    startWeightKg: null,
    endWeightKg: null,
    weightChangeKg: null,
    dailySummaries: [],
  }
}

describe('multi-user app', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    apiMocks.getUsers.mockResolvedValue(users)
    apiMocks.getProfile.mockImplementation((userId: number) => Promise.resolve(profile(userId)))
    apiMocks.getDailySummary.mockResolvedValue(dailySummary())
    apiMocks.getRecentSummaries.mockResolvedValue([])
    apiMocks.getRecentWeightRecords.mockResolvedValue([])
    apiMocks.getPeriodReport.mockImplementation((_userId: number, days: number) => Promise.resolve(report(days)))
  })

  it('restores the saved user and reloads all data after switching users', async () => {
    localStorage.setItem('weight-loss-tracker:selected-user-id', '2')
    const user = userEvent.setup()
    render(<App />)

    const selector = await screen.findByRole('combobox', { name: '当前用户' })
    await waitFor(() => expect(selector).toHaveValue('2'))
    expect(apiMocks.getProfile).toHaveBeenCalledWith(2)
    expect(apiMocks.getDailySummary).toHaveBeenCalledWith(2, expect.any(String))

    await user.selectOptions(selector, '1')

    await waitFor(() => expect(apiMocks.getProfile).toHaveBeenCalledWith(1))
    expect(apiMocks.getDailySummary).toHaveBeenCalledWith(1, expect.any(String))
    expect(apiMocks.getPeriodReport).toHaveBeenCalledWith(1, 30)
    expect(localStorage.getItem('weight-loss-tracker:selected-user-id')).toBe('1')
  })
})
