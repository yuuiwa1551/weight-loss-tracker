import { useEffect, useState, type FormEvent } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { api, type DailySummary, type RecentSummary, type UserProfile, type WeightRecord } from './api'
import { AppShell } from './components'
import { DashboardPage, ExerciseRecordPage, FoodRecordPage, ProfilePage, WeightRecordPage } from './pages'
import { getPageByPath, pages } from './routes'
import type { ExerciseFormState, FoodFormState, Notice, ProfileFormState, WeightFormState } from './types'
import { getErrorMessage, today } from './utils'
import './App.css'

function initialFoodForm(date: string): FoodFormState {
  return {
    recordDate: date,
    mealType: 'LUNCH',
    foodName: '',
    calories: '',
    protein: '',
    fat: '',
    carbohydrate: '',
    note: '',
  }
}

function initialExerciseForm(date: string): ExerciseFormState {
  return {
    recordDate: date,
    exerciseType: 'Cardio',
    exerciseName: '',
    durationMinutes: '',
    caloriesBurned: '',
    note: '',
  }
}

function initialWeightForm(date: string): WeightFormState {
  return {
    recordDate: date,
    weightKg: '',
    bodyFatPercentage: '',
    note: '',
  }
}

function profileToForm(profile: UserProfile): ProfileFormState {
  return {
    nickname: profile.nickname,
    heightCm: String(profile.heightCm),
    currentWeightKg: String(profile.currentWeightKg),
    targetWeightKg: String(profile.targetWeightKg),
    dailyCalorieGoal: String(profile.dailyCalorieGoal),
  }
}

function toNumber(value: string) {
  return Number(value || 0)
}

function optionalNote(value: string) {
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : null
}

function AppContent() {
  const location = useLocation()
  const activePage = getPageByPath(location.pathname) ?? pages[0]
  const [selectedDate, setSelectedDate] = useState(today())
  const [dailySummary, setDailySummary] = useState<DailySummary | null>(null)
  const [recentSummaries, setRecentSummaries] = useState<RecentSummary[]>([])
  const [weightRecords, setWeightRecords] = useState<WeightRecord[]>([])
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [foodForm, setFoodForm] = useState<FoodFormState>(() => initialFoodForm(selectedDate))
  const [exerciseForm, setExerciseForm] = useState<ExerciseFormState>(() => initialExerciseForm(selectedDate))
  const [weightForm, setWeightForm] = useState<WeightFormState>(() => initialWeightForm(selectedDate))
  const [profileForm, setProfileForm] = useState<ProfileFormState | null>(null)
  const [loadingDashboard, setLoadingDashboard] = useState(true)
  const [loadingWeightRecords, setLoadingWeightRecords] = useState(true)
  const [loadingProfile, setLoadingProfile] = useState(true)
  const [saving, setSaving] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(null)

  async function loadDashboard(date = selectedDate) {
    setLoadingDashboard(true)
    try {
      const [daily, recent] = await Promise.all([api.getDailySummary(date), api.getRecentSummaries(7)])
      setDailySummary(daily)
      setRecentSummaries(recent)
    } catch (error) {
      setDailySummary(null)
      setRecentSummaries([])
      setNotice({ type: 'error', message: getErrorMessage(error, '仪表盘加载失败') })
    } finally {
      setLoadingDashboard(false)
    }
  }

  async function loadWeightRecords() {
    setLoadingWeightRecords(true)
    try {
      setWeightRecords(await api.getRecentWeightRecords(30))
    } catch (error) {
      setWeightRecords([])
      setNotice({ type: 'error', message: getErrorMessage(error, '体重记录加载失败') })
    } finally {
      setLoadingWeightRecords(false)
    }
  }

  function handleSelectedDateChange(nextDate: string) {
    setLoadingDashboard(true)
    setSelectedDate(nextDate)
    setFoodForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
    setExerciseForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
    setWeightForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
  }

  useEffect(() => {
    let ignore = false

    async function load() {
      try {
        const [daily, recent] = await Promise.all([api.getDailySummary(selectedDate), api.getRecentSummaries(7)])
        if (ignore) return
        setDailySummary(daily)
        setRecentSummaries(recent)
      } catch (error) {
        if (ignore) return
        setDailySummary(null)
        setRecentSummaries([])
        setNotice({ type: 'error', message: getErrorMessage(error, '仪表盘加载失败') })
      } finally {
        if (!ignore) setLoadingDashboard(false)
      }
    }

    void load()

    return () => {
      ignore = true
    }
  }, [selectedDate])

  useEffect(() => {
    let ignore = false

    async function load() {
      try {
        const nextProfile = await api.getProfile()
        if (ignore) return
        setProfile(nextProfile)
        setProfileForm(profileToForm(nextProfile))
      } catch (error) {
        if (ignore) return
        setProfile(null)
        setProfileForm(null)
        setNotice({ type: 'error', message: getErrorMessage(error, '资料加载失败') })
      } finally {
        if (!ignore) setLoadingProfile(false)
      }
    }

    void load()

    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false

    async function load() {
      try {
        const records = await api.getRecentWeightRecords(30)
        if (ignore) return
        setWeightRecords(records)
      } catch (error) {
        if (ignore) return
        setWeightRecords([])
        setNotice({ type: 'error', message: getErrorMessage(error, '体重记录加载失败') })
      } finally {
        if (!ignore) setLoadingWeightRecords(false)
      }
    }

    void load()

    return () => {
      ignore = true
    }
  }, [])

  async function handleFoodSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setNotice(null)
    try {
      const recordDate = foodForm.recordDate
      await api.createFoodRecord({
        recordDate,
        mealType: foodForm.mealType,
        foodName: foodForm.foodName.trim(),
        calories: toNumber(foodForm.calories),
        protein: toNumber(foodForm.protein),
        fat: toNumber(foodForm.fat),
        carbohydrate: toNumber(foodForm.carbohydrate),
        note: optionalNote(foodForm.note),
      })
      setFoodForm(initialFoodForm(recordDate))
      setSelectedDate(recordDate)
      await loadDashboard(recordDate)
      setNotice({ type: 'success', message: '食物记录已保存' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '食物记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handleExerciseSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setNotice(null)
    try {
      const recordDate = exerciseForm.recordDate
      await api.createExerciseRecord({
        recordDate,
        exerciseType: exerciseForm.exerciseType.trim(),
        exerciseName: exerciseForm.exerciseName.trim(),
        durationMinutes: toNumber(exerciseForm.durationMinutes),
        caloriesBurned: toNumber(exerciseForm.caloriesBurned),
        note: optionalNote(exerciseForm.note),
      })
      setExerciseForm(initialExerciseForm(recordDate))
      setSelectedDate(recordDate)
      await loadDashboard(recordDate)
      setNotice({ type: 'success', message: '运动记录已保存' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '运动记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handleProfileSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!profileForm) return
    setSaving(true)
    setNotice(null)
    try {
      const nextProfile = await api.updateProfile({
        nickname: profileForm.nickname.trim(),
        heightCm: toNumber(profileForm.heightCm),
        currentWeightKg: toNumber(profileForm.currentWeightKg),
        targetWeightKg: toNumber(profileForm.targetWeightKg),
        dailyCalorieGoal: toNumber(profileForm.dailyCalorieGoal),
      })
      setProfile(nextProfile)
      setProfileForm(profileToForm(nextProfile))
      await loadDashboard(selectedDate)
      setNotice({ type: 'success', message: '目标资料已更新' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '目标资料更新失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handleWeightSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSaving(true)
    setNotice(null)
    try {
      const recordDate = weightForm.recordDate
      await api.createWeightRecord({
        recordDate,
        weightKg: toNumber(weightForm.weightKg),
        bodyFatPercentage: weightForm.bodyFatPercentage ? toNumber(weightForm.bodyFatPercentage) : null,
        note: optionalNote(weightForm.note),
      })
      setWeightForm(initialWeightForm(recordDate))
      setSelectedDate(recordDate)
      await loadWeightRecords()
      setNotice({ type: 'success', message: '体重记录已保存' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '体重记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteFoodRecord(id: number) {
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteFoodRecord(id)
      await loadDashboard(selectedDate)
      setNotice({ type: 'success', message: '食物记录已删除' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteExerciseRecord(id: number) {
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteExerciseRecord(id)
      await loadDashboard(selectedDate)
      setNotice({ type: 'success', message: '运动记录已删除' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteWeightRecord(id: number) {
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteWeightRecord(id)
      await loadWeightRecords()
      setNotice({ type: 'success', message: '体重记录已删除' })
    } catch (error) {
      setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  return (
    <AppShell
      activePage={activePage}
      profile={profile}
      selectedDate={selectedDate}
      notice={notice}
      onSelectedDateChange={handleSelectedDateChange}
    >
      <Routes>
        <Route
          index
          element={
            <DashboardPage
              dailySummary={dailySummary}
              recentSummaries={recentSummaries}
              loadingDashboard={loadingDashboard}
              saving={saving}
              onDeleteFoodRecord={(id) => void deleteFoodRecord(id)}
              onDeleteExerciseRecord={(id) => void deleteExerciseRecord(id)}
            />
          }
        />
        <Route
          path="food"
          element={
            <FoodRecordPage
              foodForm={foodForm}
              dailySummary={dailySummary}
              loadingDashboard={loadingDashboard}
              saving={saving}
              onFoodFormChange={setFoodForm}
              onFoodSubmit={handleFoodSubmit}
              onDeleteFoodRecord={(id) => void deleteFoodRecord(id)}
            />
          }
        />
        <Route
          path="exercise"
          element={
            <ExerciseRecordPage
              exerciseForm={exerciseForm}
              dailySummary={dailySummary}
              loadingDashboard={loadingDashboard}
              saving={saving}
              onExerciseFormChange={setExerciseForm}
              onExerciseSubmit={handleExerciseSubmit}
              onDeleteExerciseRecord={(id) => void deleteExerciseRecord(id)}
            />
          }
        />
        <Route
          path="weight"
          element={
            <WeightRecordPage
              weightForm={weightForm}
              weightRecords={weightRecords}
              profile={profile}
              loadingWeightRecords={loadingWeightRecords}
              saving={saving}
              onWeightFormChange={setWeightForm}
              onWeightSubmit={handleWeightSubmit}
              onDeleteWeightRecord={(id) => void deleteWeightRecord(id)}
            />
          }
        />
        <Route
          path="profile"
          element={
            <ProfilePage
              profile={profile}
              profileForm={profileForm}
              loadingProfile={loadingProfile}
              saving={saving}
              onProfileFormChange={setProfileForm}
              onProfileSubmit={handleProfileSubmit}
            />
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AppShell>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  )
}

export default App