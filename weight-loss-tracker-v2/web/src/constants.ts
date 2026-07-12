import type { CalorieGoalMode, FormulaSex, GoalStatus, MealType, NonExerciseActivityLevel } from './api'

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

export const formulaSexLabels: Record<FormulaSex, string> = {
  MALE: '男性公式',
  FEMALE: '女性公式',
}

export const activityLabels: Record<NonExerciseActivityLevel, string> = {
  SEDENTARY: '久坐为主',
  LIGHT: '少量走动',
  MODERATE: '日常走动较多',
  HIGH: '日常体力活动较多',
}

export const calorieGoalModeLabels: Record<CalorieGoalMode, string> = {
  UNSET: '未设置',
  MANUAL: '手动目标',
  AUTO: '自动计划',
}
