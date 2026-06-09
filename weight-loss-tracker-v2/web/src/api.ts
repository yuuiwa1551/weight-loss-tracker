export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'
export type GoalStatus = 'UNDER' | 'MEET' | 'OVER'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface UserProfile {
  id: number
  nickname: string
  heightCm: number
  currentWeightKg: number
  targetWeightKg: number
  dailyCalorieGoal: number
  bmi: number
  weightToLoseKg: number
  createdAt: string
  updatedAt: string
}

export interface FoodRecord {
  id: number
  recordDate: string
  mealType: MealType
  foodName: string
  calories: number
  protein: number
  fat: number
  carbohydrate: number
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface ExerciseRecord {
  id: number
  recordDate: string
  exerciseType: string
  exerciseName: string
  durationMinutes: number
  caloriesBurned: number
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface DailySummary {
  date: string
  totalCaloriesConsumed: number
  totalCaloriesBurned: number
  netCalories: number
  dailyCalorieGoal: number
  calorieDifference: number
  goalStatus: GoalStatus
  totalProtein: number
  totalFat: number
  totalCarbohydrate: number
  foodRecords: FoodRecord[]
  exerciseRecords: ExerciseRecord[]
}

export interface RecentSummary {
  date: string
  totalCaloriesConsumed: number
  totalCaloriesBurned: number
  netCalories: number
  dailyCalorieGoal: number
  calorieDifference: number
  goalStatus: GoalStatus
}

export interface UpdateProfileRequest {
  nickname: string
  heightCm: number
  currentWeightKg: number
  targetWeightKg: number
  dailyCalorieGoal: number
}

export interface CreateFoodRecordRequest {
  recordDate: string
  mealType: MealType
  foodName: string
  calories: number
  protein: number
  fat: number
  carbohydrate: number
  note?: string | null
}

export interface CreateExerciseRecordRequest {
  recordDate: string
  exerciseType: string
  exerciseName: string
  durationMinutes: number
  caloriesBurned: number
  note?: string | null
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
    ...init,
  })

  const body = (await response.json().catch(() => null)) as ApiResponse<T> | null

  if (!response.ok || !body?.success) {
    throw new Error(body?.message || `Request failed with ${response.status}`)
  }

  return body.data
}

function dateQuery(date?: string) {
  return date ? `?date=${encodeURIComponent(date)}` : ''
}

export const api = {
  getProfile: () => request<UserProfile>('/profile'),
  updateProfile: (payload: UpdateProfileRequest) =>
    request<UserProfile>('/profile', {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  createFoodRecord: (payload: CreateFoodRecordRequest) =>
    request<FoodRecord>('/food-records', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  deleteFoodRecord: (id: number) => request<null>(`/food-records/${id}`, { method: 'DELETE' }),
  createExerciseRecord: (payload: CreateExerciseRecordRequest) =>
    request<ExerciseRecord>('/exercise-records', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  deleteExerciseRecord: (id: number) => request<null>(`/exercise-records/${id}`, { method: 'DELETE' }),
  getDailySummary: (date?: string) => request<DailySummary>(`/summaries/daily${dateQuery(date)}`),
  getRecentSummaries: (days = 7) => request<RecentSummary[]>(`/summaries/recent?days=${days}`),
}