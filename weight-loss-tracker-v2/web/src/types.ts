import type {
  CalorieGoalMode,
  EnergyPlanPreview,
  EnergyPlanPreviewRequest,
  ExerciseRecordPreview,
  FormulaSex,
  MealType,
  NonExerciseActivityLevel,
  FoodRecordPreview,
  CreateExerciseRecordRequest,
  CreateFoodRecordRequest,
} from './api'

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
  ageYears: string
  formulaSex: FormulaSex | ''
  nonExerciseActivityLevel: NonExerciseActivityLevel | ''
  calorieGoalMode: CalorieGoalMode
}

export interface EnergyPlanFormState {
  mode: 'DEFAULT_RATE' | 'EXPLICIT' | 'TARGET_PERIOD'
  dailyDeficitCalories: string
  targetPeriodDays: string
}

export type PendingConfirmation =
  | { kind: 'food'; preview: FoodRecordPreview; request: CreateFoodRecordRequest }
  | { kind: 'exercise'; preview: ExerciseRecordPreview; request: CreateExerciseRecordRequest }
  | {
      kind: 'energyPlan'
      preview: EnergyPlanPreview
      request: EnergyPlanPreviewRequest
      clientRequestId: string
    }

export interface WeightFormState {
  recordDate: string
  weightKg: string
  bodyFatPercentage: string
  note: string
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
