import type { GoalStatus, MealType } from './api'

export const mealLabels: Record<MealType, string> = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '加餐',
}

export const goalLabels: Record<GoalStatus, string> = {
  UNSET: '尚未设置目标',
  UNDER: '低于目标',
  MEET: '接近目标',
  OVER: '超过目标',
}
