import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import type { AppUser, DailySummary, PeriodReport, UserProfile } from './api'

const apiMocks = vi.hoisted(() => ({
  getUsers: vi.fn(),
  getProfile: vi.fn(),
  updateProfile: vi.fn(),
  previewEnergyPlan: vi.fn(),
  confirmEnergyPlan: vi.fn(),
  getActiveEnergyPlan: vi.fn(),
  getDailyEnergyBudget: vi.fn(),
  previewFoodRecord: vi.fn(),
  createFoodRecord: vi.fn(),
  deleteFoodRecord: vi.fn(),
  previewExerciseRecord: vi.fn(),
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
    ageYears: null,
    formulaSex: null,
    nonExerciseActivityLevel: null,
    calorieGoalMode: 'UNSET',
    bmi: null,
    weightToLoseKg: null,
    profileComplete: false,
    missingFields: ['heightCm', 'currentWeightKg', 'targetWeightKg', 'dailyCalorieGoal'],
    energyProfileComplete: false,
    energyMissingFields: ['ageYears', 'formulaSex', 'heightCm', 'currentWeightKg', 'nonExerciseActivityLevel'],
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
    energyBudget: {
      date: '2026-07-12',
      restingEnergyCalories: null,
      baselineExpenditureCalories: null,
      exerciseCaloriesBurned: 0,
      estimatedTotalExpenditureCalories: null,
      baseIntakeTargetCalories: null,
      todayIntakeBudgetCalories: null,
      caloriesConsumed: 0,
      remainingIntakeCalories: null,
      projectedDeficitCalories: null,
      goalMode: 'UNSET',
      calculationMethod: null,
      calculationVersion: null,
    },
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
    apiMocks.getActiveEnergyPlan.mockResolvedValue(null)
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

  it('previews food values and only writes after explicit confirmation', async () => {
    const user = userEvent.setup()
    const budget = {
      ...dailySummary().energyBudget,
      baseIntakeTargetCalories: 1900,
      todayIntakeBudgetCalories: 1900,
      caloriesConsumed: 620,
      remainingIntakeCalories: 1280,
      goalMode: 'MANUAL' as const,
    }
    apiMocks.previewFoodRecord.mockResolvedValue({
      recordDate: '2026-07-12',
      mealType: 'LUNCH',
      foodName: '鸡肉沙拉',
      calories: 620,
      protein: 0,
      fat: 0,
      carbohydrate: 0,
      note: null,
      source: 'WEB',
      nutritionSource: 'USER_PROVIDED',
      estimationNote: null,
      projectedEnergyBudget: budget,
      previewFingerprint: 'food-fingerprint',
    })
    apiMocks.createFoodRecord.mockResolvedValue({
      id: 10,
      recordDate: '2026-07-12',
      mealType: 'LUNCH',
      foodName: '鸡肉沙拉',
      calories: 620,
      protein: 0,
      fat: 0,
      carbohydrate: 0,
      note: null,
      source: 'WEB',
      clientRequestId: 'web:food:test',
      nutritionSource: 'USER_PROVIDED',
      estimationNote: null,
      createdAt: '',
      updatedAt: '',
      energyBudget: budget,
    })
    render(<App />)

    await user.click(await screen.findByRole('link', { name: /食物记录/ }))
    await user.type(screen.getByLabelText('食物名称'), '鸡肉沙拉')
    await user.type(screen.getByLabelText('热量'), '620')
    await user.click(screen.getByRole('button', { name: '预览' }))

    const foodDialog = await screen.findByRole('dialog', { name: '确认食物记录' })
    expect(within(foodDialog).getByText('剩余可摄入').nextSibling).toHaveTextContent('1280 kcal')
    expect(apiMocks.createFoodRecord).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: '确认写入' }))
    await waitFor(() => expect(apiMocks.createFoodRecord).toHaveBeenCalledTimes(1))
    expect(apiMocks.createFoodRecord).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ previewFingerprint: 'food-fingerprint', clientRequestId: expect.stringMatching(/^web:food:/) }),
    )
    expect(await screen.findByText('食物记录已保存，剩余 1280 kcal')).toBeInTheDocument()
  })

  it('previews and confirms the backend energy plan calculation', async () => {
    const user = userEvent.setup()
    apiMocks.getProfile.mockResolvedValue({
      ...profile(1),
      heightCm: 165,
      currentWeightKg: 52,
      targetWeightKg: 48,
      ageYears: 24,
      formulaSex: 'FEMALE',
      nonExerciseActivityLevel: 'LIGHT',
      energyProfileComplete: true,
      energyMissingFields: [],
    })
    const calculation = {
      calculationMethod: 'MIFFLIN_ST_JEOR' as const,
      calculationVersion: 'P6_V1',
      deficitMode: 'DEFAULT_RATE' as const,
      ageYears: 24,
      formulaSex: 'FEMALE' as const,
      heightCm: 165,
      weightKg: 52,
      targetWeightKg: 48,
      nonExerciseActivityLevel: 'LIGHT' as const,
      targetPeriodDays: null,
      restingEnergyCalories: 1270,
      baselineExpenditureCalories: 1651,
      dailyDeficitCalories: 248,
      baseIntakeTargetCalories: 1403,
      profileUpdatedAt: '2026-07-12T12:00:00',
    }
    apiMocks.previewEnergyPlan.mockResolvedValue({ calculation, previewFingerprint: 'plan-fingerprint' })
    apiMocks.confirmEnergyPlan.mockResolvedValue({
      id: 20,
      calculation,
      status: 'ACTIVE',
      effectiveFrom: '2026-07-12',
      clientRequestId: 'web:energy-plan:test',
      createdAt: '',
      updatedAt: '',
    })
    render(<App />)

    await user.click(await screen.findByRole('link', { name: /目标资料/ }))
    await user.click(await screen.findByRole('button', { name: '预览' }))

    const planDialog = await screen.findByRole('dialog', { name: '确认能量计划' })
    expect(within(planDialog).getByText('静息消耗').nextSibling).toHaveTextContent('1270 kcal')
    expect(apiMocks.confirmEnergyPlan).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: '确认写入' }))
    await waitFor(() => expect(apiMocks.confirmEnergyPlan).toHaveBeenCalledTimes(1))
    expect(apiMocks.confirmEnergyPlan).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ previewFingerprint: 'plan-fingerprint', calculation: { dailyDeficitCalories: null, targetPeriodDays: null } }),
    )
  })
})
