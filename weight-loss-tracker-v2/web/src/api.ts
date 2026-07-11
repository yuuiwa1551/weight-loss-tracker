export type MealType = 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'SNACK'
export type GoalStatus = 'UNSET' | 'UNDER' | 'MEET' | 'OVER'
export type RecordSource = 'WEB' | 'ASTRBOT'
export type NutritionSource = 'USER_PROVIDED' | 'LLM_ESTIMATE'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface AppUser {
  id: number
  platform: string
  username: string
  displayName: string | null
  createdAt: string
  updatedAt: string
}

export interface UserProfile {
  id: number
  nickname: string | null
  heightCm: number | null
  currentWeightKg: number | null
  targetWeightKg: number | null
  dailyCalorieGoal: number | null
  bmi: number | null
  weightToLoseKg: number | null
  profileComplete: boolean
  missingFields: string[]
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
  source: RecordSource
  clientRequestId: string | null
  nutritionSource: NutritionSource
  estimationNote: string | null
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
  source: RecordSource
  clientRequestId: string | null
  createdAt: string
  updatedAt: string
}

export interface WeightRecord {
  id: number
  recordDate: string
  weightKg: number
  bodyFatPercentage: number | null
  note: string | null
  source: RecordSource
  clientRequestId: string | null
  createdAt: string
  updatedAt: string
}

export interface DailySummary {
  date: string
  totalCaloriesConsumed: number
  totalCaloriesBurned: number
  netCalories: number
  dailyCalorieGoal: number | null
  calorieDifference: number | null
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
  dailyCalorieGoal: number | null
  calorieDifference: number | null
  goalStatus: GoalStatus
}

export interface PeriodReport {
  startDate: string
  endDate: string
  days: number
  totalCaloriesConsumed: number
  totalCaloriesBurned: number
  netCalories: number
  averageCaloriesConsumed: number
  averageCaloriesBurned: number
  averageNetCalories: number
  averageProtein: number
  averageFat: number
  averageCarbohydrate: number
  dailyCalorieGoal: number | null
  daysUnderGoal: number
  daysMeetGoal: number
  daysOverGoal: number
  startWeightKg: number | null
  endWeightKg: number | null
  weightChangeKg: number | null
  dailySummaries: RecentSummary[]
}

export interface UpdateProfileRequest {
  nickname: string | null
  heightCm: number | null
  currentWeightKg: number | null
  targetWeightKg: number | null
  dailyCalorieGoal: number | null
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

export interface CreateWeightRecordRequest {
  recordDate: string
  weightKg: number
  bodyFatPercentage?: number | null
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

function userPath(userId: number, path: string) {
  return `/users/${userId}${path}`
}

export const api = {
  getUsers: () => request<AppUser[]>('/users'),
  getProfile: (userId: number) => request<UserProfile>(userPath(userId, '/profile')),
  updateProfile: (userId: number, payload: UpdateProfileRequest) =>
    request<UserProfile>(userPath(userId, '/profile'), {
      method: 'PUT',
      body: JSON.stringify(payload),
    }),
  createFoodRecord: (userId: number, payload: CreateFoodRecordRequest) =>
    request<FoodRecord>(userPath(userId, '/food-records'), {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  deleteFoodRecord: (userId: number, id: number) => request<null>(userPath(userId, `/food-records/${id}`), { method: 'DELETE' }),
  createExerciseRecord: (userId: number, payload: CreateExerciseRecordRequest) =>
    request<ExerciseRecord>(userPath(userId, '/exercise-records'), {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  deleteExerciseRecord: (userId: number, id: number) => request<null>(userPath(userId, `/exercise-records/${id}`), { method: 'DELETE' }),
  getRecentWeightRecords: (userId: number, days = 30) => request<WeightRecord[]>(userPath(userId, `/weight-records/recent?days=${days}`)),
  createWeightRecord: (userId: number, payload: CreateWeightRecordRequest) =>
    request<WeightRecord>(userPath(userId, '/weight-records'), {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
  deleteWeightRecord: (userId: number, id: number) => request<null>(userPath(userId, `/weight-records/${id}`), { method: 'DELETE' }),
  getPeriodReport: (userId: number, days: number) => request<PeriodReport>(userPath(userId, `/reports/overview?days=${days}`)),
  getDailySummary: (userId: number, date?: string) => request<DailySummary>(userPath(userId, `/summaries/daily${dateQuery(date)}`)),
  getRecentSummaries: (userId: number, days = 7) => request<RecentSummary[]>(userPath(userId, `/summaries/recent?days=${days}`)),
}
