import type { MealType } from './api'

export interface FoodFormState {
  recordDate: string
  mealType: MealType
  foodName: string
  calories: string
  protein: string
  fat: string
  carbohydrate: string
  note: string
}

export interface ExerciseFormState {
  recordDate: string
  exerciseType: string
  exerciseName: string
  durationMinutes: string
  caloriesBurned: string
  note: string
}

export interface ProfileFormState {
  nickname: string
  heightCm: string
  currentWeightKg: string
  targetWeightKg: string
  dailyCalorieGoal: string
}

export interface Notice {
  type: 'success' | 'error'
  message: string
}

export interface RecordListRow {
  id: number
  primary: string
  secondary: string
  value: string
  onDelete: () => void
}