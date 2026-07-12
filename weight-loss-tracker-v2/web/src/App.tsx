import { useEffect, useRef, useState, type FormEvent } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import {
  api,
  type AppUser,
  type CreateExerciseRecordRequest,
  type CreateFoodRecordRequest,
  type DailySummary,
  type EnergyPlan,
  type EnergyPlanPreviewRequest,
  type PeriodReport,
  type RecentSummary,
  type UserProfile,
  type WeightRecord,
} from './api'
import { AppShell, ConfirmationDialog } from './components'
import { DashboardPage, ExerciseRecordPage, FoodRecordPage, ProfilePage, ReportsPage, WeightRecordPage } from './pages'
import { getPageByPath, pages } from './routes'
import type { EnergyPlanFormState, ExerciseFormState, FoodFormState, Notice, PendingConfirmation, ProfileFormState, WeightFormState } from './types'
import { createClientRequestId, getErrorMessage, selectInitialUserId, today } from './utils'
import './App.css'

const SELECTED_USER_KEY = 'weight-loss-tracker:selected-user-id'

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
    nickname: profile.nickname ?? '',
    heightCm: profile.heightCm == null ? '' : String(profile.heightCm),
    currentWeightKg: profile.currentWeightKg == null ? '' : String(profile.currentWeightKg),
    targetWeightKg: profile.targetWeightKg == null ? '' : String(profile.targetWeightKg),
    dailyCalorieGoal: profile.calorieGoalMode !== 'MANUAL' || profile.dailyCalorieGoal == null ? '' : String(profile.dailyCalorieGoal),
    ageYears: profile.ageYears == null ? '' : String(profile.ageYears),
    formulaSex: profile.formulaSex ?? '',
    nonExerciseActivityLevel: profile.nonExerciseActivityLevel ?? '',
    calorieGoalMode: profile.calorieGoalMode,
  }
}

const initialPlanForm: EnergyPlanFormState = {
  mode: 'DEFAULT_RATE',
  dailyDeficitCalories: '',
  targetPeriodDays: '',
}

function toNumber(value: string) {
  return Number(value || 0)
}

function optionalNumber(value: string) {
  return value.trim() === '' ? null : Number(value)
}

function optionalText(value: string) {
  const trimmed = value.trim()
  return trimmed.length > 0 ? trimmed : null
}

function AppContent() {
  const location = useLocation()
  const activePage = getPageByPath(location.pathname) ?? pages[0]
  const [users, setUsers] = useState<AppUser[]>([])
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null)
  const selectedUserIdRef = useRef<number | null>(null)
  const [selectedDate, setSelectedDate] = useState(today())
  const [dailySummary, setDailySummary] = useState<DailySummary | null>(null)
  const [recentSummaries, setRecentSummaries] = useState<RecentSummary[]>([])
  const [weightRecords, setWeightRecords] = useState<WeightRecord[]>([])
  const [weeklyReport, setWeeklyReport] = useState<PeriodReport | null>(null)
  const [monthlyReport, setMonthlyReport] = useState<PeriodReport | null>(null)
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [activePlan, setActivePlan] = useState<EnergyPlan | null>(null)
  const [foodForm, setFoodForm] = useState<FoodFormState>(() => initialFoodForm(selectedDate))
  const [exerciseForm, setExerciseForm] = useState<ExerciseFormState>(() => initialExerciseForm(selectedDate))
  const [weightForm, setWeightForm] = useState<WeightFormState>(() => initialWeightForm(selectedDate))
  const [profileForm, setProfileForm] = useState<ProfileFormState | null>(null)
  const [planForm, setPlanForm] = useState<EnergyPlanFormState>(initialPlanForm)
  const [pendingConfirmation, setPendingConfirmation] = useState<PendingConfirmation | null>(null)
  const [loadingUsers, setLoadingUsers] = useState(true)
  const [loadingDashboard, setLoadingDashboard] = useState(false)
  const [loadingWeightRecords, setLoadingWeightRecords] = useState(false)
  const [loadingReports, setLoadingReports] = useState(false)
  const [loadingProfile, setLoadingProfile] = useState(false)
  const [saving, setSaving] = useState(false)
  const [notice, setNotice] = useState<Notice | null>(null)

  useEffect(() => {
    selectedUserIdRef.current = selectedUserId
  }, [selectedUserId])

  async function loadDashboard(userId: number, date = selectedDate) {
    setLoadingDashboard(true)
    try {
      const [daily, recent] = await Promise.all([api.getDailySummary(userId, date), api.getRecentSummaries(userId, 7)])
      if (selectedUserIdRef.current !== userId) return
      setDailySummary(daily)
      setRecentSummaries(recent)
    } catch (error) {
      if (selectedUserIdRef.current !== userId) return
      setDailySummary(null)
      setRecentSummaries([])
      setNotice({ type: 'error', message: getErrorMessage(error, '仪表盘加载失败') })
    } finally {
      if (selectedUserIdRef.current === userId) setLoadingDashboard(false)
    }
  }

  async function loadWeightRecords(userId: number) {
    setLoadingWeightRecords(true)
    try {
      const records = await api.getRecentWeightRecords(userId, 30)
      if (selectedUserIdRef.current !== userId) return
      setWeightRecords(records)
    } catch (error) {
      if (selectedUserIdRef.current !== userId) return
      setWeightRecords([])
      setNotice({ type: 'error', message: getErrorMessage(error, '体重记录加载失败') })
    } finally {
      if (selectedUserIdRef.current === userId) setLoadingWeightRecords(false)
    }
  }

  async function loadReports(userId: number) {
    setLoadingReports(true)
    try {
      const [weekly, monthly] = await Promise.all([api.getPeriodReport(userId, 7), api.getPeriodReport(userId, 30)])
      if (selectedUserIdRef.current !== userId) return
      setWeeklyReport(weekly)
      setMonthlyReport(monthly)
    } catch (error) {
      if (selectedUserIdRef.current !== userId) return
      setWeeklyReport(null)
      setMonthlyReport(null)
      setNotice({ type: 'error', message: getErrorMessage(error, '周期报表加载失败') })
    } finally {
      if (selectedUserIdRef.current === userId) setLoadingReports(false)
    }
  }

  async function loadProfile(userId: number) {
    setLoadingProfile(true)
    try {
      const nextProfile = await api.getProfile(userId)
      const nextPlan = nextProfile.calorieGoalMode === 'AUTO' ? await api.getActiveEnergyPlan(userId) : null
      if (selectedUserIdRef.current !== userId) return
      setProfile(nextProfile)
      setActivePlan(nextPlan)
      setProfileForm(profileToForm(nextProfile))
    } catch (error) {
      if (selectedUserIdRef.current !== userId) return
      setProfile(null)
      setActivePlan(null)
      setProfileForm(null)
      setNotice({ type: 'error', message: getErrorMessage(error, '资料加载失败') })
    } finally {
      if (selectedUserIdRef.current === userId) setLoadingProfile(false)
    }
  }

  function handleSelectedDateChange(nextDate: string) {
    if (selectedUserId !== null) setLoadingDashboard(true)
    setSelectedDate(nextDate)
    setFoodForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
    setExerciseForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
    setWeightForm((currentForm) => ({ ...currentForm, recordDate: nextDate }))
    setPendingConfirmation(null)
  }

  function handleSelectedUserChange(userId: number) {
    if (!users.some((user) => user.id === userId)) return
    localStorage.setItem(SELECTED_USER_KEY, String(userId))
    setNotice(null)
    setDailySummary(null)
    setRecentSummaries([])
    setWeightRecords([])
    setWeeklyReport(null)
    setMonthlyReport(null)
    setProfile(null)
    setActivePlan(null)
    setProfileForm(null)
    setPendingConfirmation(null)
    setLoadingDashboard(true)
    setLoadingWeightRecords(true)
    setLoadingReports(true)
    setLoadingProfile(true)
    setSelectedUserId(userId)
  }

  useEffect(() => {
    let ignore = false

    async function load() {
      try {
        const nextUsers = await api.getUsers()
        if (ignore) return
        const nextUserId = selectInitialUserId(nextUsers, localStorage.getItem(SELECTED_USER_KEY))
        setUsers(nextUsers)
        setLoadingDashboard(nextUserId !== null)
        setLoadingWeightRecords(nextUserId !== null)
        setLoadingReports(nextUserId !== null)
        setLoadingProfile(nextUserId !== null)
        setSelectedUserId(nextUserId)
        if (nextUserId !== null) localStorage.setItem(SELECTED_USER_KEY, String(nextUserId))
      } catch (error) {
        if (ignore) return
        setUsers([])
        setSelectedUserId(null)
        setNotice({ type: 'error', message: getErrorMessage(error, '用户列表加载失败') })
      } finally {
        if (!ignore) setLoadingUsers(false)
      }
    }

    void load()
    return () => {
      ignore = true
    }
  }, [])

  useEffect(() => {
    let ignore = false
    if (selectedUserId === null) return

    Promise.all([api.getDailySummary(selectedUserId, selectedDate), api.getRecentSummaries(selectedUserId, 7)])
      .then(([daily, recent]) => {
        if (ignore) return
        setDailySummary(daily)
        setRecentSummaries(recent)
      })
      .catch((error: unknown) => {
        if (ignore) return
        setDailySummary(null)
        setRecentSummaries([])
        setNotice({ type: 'error', message: getErrorMessage(error, '仪表盘加载失败') })
      })
      .finally(() => {
        if (!ignore) setLoadingDashboard(false)
      })

    return () => {
      ignore = true
    }
  }, [selectedDate, selectedUserId])

  useEffect(() => {
    let ignore = false
    if (selectedUserId === null) return

    api.getProfile(selectedUserId)
      .then(async (nextProfile) => {
        const nextPlan = nextProfile.calorieGoalMode === 'AUTO' ? await api.getActiveEnergyPlan(selectedUserId) : null
        if (ignore) return
        setProfile(nextProfile)
        setActivePlan(nextPlan)
        setProfileForm(profileToForm(nextProfile))
      })
      .catch((error: unknown) => {
        if (ignore) return
        setProfile(null)
        setActivePlan(null)
        setProfileForm(null)
        setNotice({ type: 'error', message: getErrorMessage(error, '资料加载失败') })
      })
      .finally(() => {
        if (!ignore) setLoadingProfile(false)
      })

    return () => {
      ignore = true
    }
  }, [selectedUserId])

  useEffect(() => {
    let ignore = false
    if (selectedUserId === null) return

    Promise.all([api.getPeriodReport(selectedUserId, 7), api.getPeriodReport(selectedUserId, 30)])
      .then(([weekly, monthly]) => {
        if (ignore) return
        setWeeklyReport(weekly)
        setMonthlyReport(monthly)
      })
      .catch((error: unknown) => {
        if (ignore) return
        setWeeklyReport(null)
        setMonthlyReport(null)
        setNotice({ type: 'error', message: getErrorMessage(error, '周期报表加载失败') })
      })
      .finally(() => {
        if (!ignore) setLoadingReports(false)
      })

    return () => {
      ignore = true
    }
  }, [selectedUserId])

  useEffect(() => {
    let ignore = false
    if (selectedUserId === null) return

    api.getRecentWeightRecords(selectedUserId, 30)
      .then((records) => {
        if (!ignore) setWeightRecords(records)
      })
      .catch((error: unknown) => {
        if (ignore) return
        setWeightRecords([])
        setNotice({ type: 'error', message: getErrorMessage(error, '体重记录加载失败') })
      })
      .finally(() => {
        if (!ignore) setLoadingWeightRecords(false)
      })

    return () => {
      ignore = true
    }
  }, [selectedUserId])

  async function handleFoodSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      const request: CreateFoodRecordRequest = {
        recordDate: foodForm.recordDate,
        mealType: foodForm.mealType,
        foodName: foodForm.foodName.trim(),
        calories: toNumber(foodForm.calories),
        protein: toNumber(foodForm.protein),
        fat: toNumber(foodForm.fat),
        carbohydrate: toNumber(foodForm.carbohydrate),
        note: optionalText(foodForm.note),
        source: 'WEB',
        clientRequestId: createClientRequestId('food'),
        nutritionSource: 'USER_PROVIDED',
        estimationNote: null,
        previewFingerprint: null,
      }
      const preview = await api.previewFoodRecord(userId, request)
      if (selectedUserIdRef.current === userId) setPendingConfirmation({ kind: 'food', preview, request })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '食物记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handleExerciseSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      const request: CreateExerciseRecordRequest = {
        recordDate: exerciseForm.recordDate,
        exerciseType: exerciseForm.exerciseType.trim(),
        exerciseName: exerciseForm.exerciseName.trim(),
        durationMinutes: toNumber(exerciseForm.durationMinutes),
        caloriesBurned: toNumber(exerciseForm.caloriesBurned),
        note: optionalText(exerciseForm.note),
        source: 'WEB',
        clientRequestId: createClientRequestId('exercise'),
        previewFingerprint: null,
      }
      const preview = await api.previewExerciseRecord(userId, request)
      if (selectedUserIdRef.current === userId) setPendingConfirmation({ kind: 'exercise', preview, request })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '运动记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handleProfileSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!profileForm || selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      const nextProfile = await api.updateProfile(userId, {
        nickname: optionalText(profileForm.nickname),
        heightCm: optionalNumber(profileForm.heightCm),
        currentWeightKg: optionalNumber(profileForm.currentWeightKg),
        targetWeightKg: optionalNumber(profileForm.targetWeightKg),
        dailyCalorieGoal: profileForm.calorieGoalMode === 'MANUAL' ? optionalNumber(profileForm.dailyCalorieGoal) : null,
        ageYears: optionalNumber(profileForm.ageYears),
        formulaSex: profileForm.formulaSex || null,
        nonExerciseActivityLevel: profileForm.nonExerciseActivityLevel || null,
        calorieGoalMode: profileForm.calorieGoalMode,
      })
      if (selectedUserIdRef.current === userId) {
        setProfile(nextProfile)
        setProfileForm(profileToForm(nextProfile))
      }
      await Promise.all([loadDashboard(userId, selectedDate), loadReports(userId)])
      if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '目标资料已更新' })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '目标资料更新失败') })
    } finally {
      setSaving(false)
    }
  }

  async function handlePlanSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (selectedUserId === null) return
    const userId = selectedUserId
    const request: EnergyPlanPreviewRequest = {
      dailyDeficitCalories: planForm.mode === 'EXPLICIT' ? optionalNumber(planForm.dailyDeficitCalories) : null,
      targetPeriodDays: planForm.mode === 'TARGET_PERIOD' ? optionalNumber(planForm.targetPeriodDays) : null,
    }
    setSaving(true)
    setNotice(null)
    try {
      const preview = await api.previewEnergyPlan(userId, request)
      if (selectedUserIdRef.current === userId) {
        setPendingConfirmation({
          kind: 'energyPlan',
          preview,
          request,
          clientRequestId: createClientRequestId('energy-plan'),
        })
      }
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '计划预览失败') })
    } finally {
      setSaving(false)
    }
  }

  async function confirmPending() {
    if (!pendingConfirmation || selectedUserId === null) return
    const userId = selectedUserId
    const pending = pendingConfirmation
    setSaving(true)
    setNotice(null)
    try {
      if (pending.kind === 'food') {
        const saved = await api.createFoodRecord(userId, {
          ...pending.request,
          previewFingerprint: pending.preview.previewFingerprint,
        })
        setFoodForm(initialFoodForm(pending.request.recordDate))
        setSelectedDate(pending.request.recordDate)
        await Promise.all([loadDashboard(userId, pending.request.recordDate), loadReports(userId)])
        const remaining = saved.energyBudget?.remainingIntakeCalories
        if (selectedUserIdRef.current === userId) {
          setNotice({ type: 'success', message: remaining == null ? '食物记录已保存' : `食物记录已保存，剩余 ${remaining} kcal` })
        }
      } else if (pending.kind === 'exercise') {
        const saved = await api.createExerciseRecord(userId, {
          ...pending.request,
          previewFingerprint: pending.preview.previewFingerprint,
        })
        setExerciseForm(initialExerciseForm(pending.request.recordDate))
        setSelectedDate(pending.request.recordDate)
        await Promise.all([loadDashboard(userId, pending.request.recordDate), loadReports(userId)])
        const remaining = saved.energyBudget?.remainingIntakeCalories
        if (selectedUserIdRef.current === userId) {
          setNotice({ type: 'success', message: remaining == null ? '运动记录已保存' : `运动记录已保存，剩余 ${remaining} kcal` })
        }
      } else {
        const saved = await api.confirmEnergyPlan(userId, {
          calculation: pending.request,
          previewFingerprint: pending.preview.previewFingerprint,
          clientRequestId: pending.clientRequestId,
        })
        if (selectedUserIdRef.current === userId) setActivePlan(saved)
        await Promise.all([loadProfile(userId), loadDashboard(userId, selectedDate), loadReports(userId)])
        if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '能量计划已启用' })
      }
      if (selectedUserIdRef.current === userId) setPendingConfirmation(null)
    } catch (error) {
      if (selectedUserIdRef.current === userId) {
        setPendingConfirmation(null)
        setNotice({ type: 'error', message: getErrorMessage(error, '确认写入失败，请重新预览') })
      }
    } finally {
      setSaving(false)
    }
  }

  async function handleWeightSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      const recordDate = weightForm.recordDate
      await api.createWeightRecord(userId, {
        recordDate,
        weightKg: toNumber(weightForm.weightKg),
        bodyFatPercentage: optionalNumber(weightForm.bodyFatPercentage),
        note: optionalText(weightForm.note),
      })
      setWeightForm(initialWeightForm(recordDate))
      setSelectedDate(recordDate)
      await Promise.all([loadWeightRecords(userId), loadReports(userId), loadProfile(userId)])
      if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '体重记录已保存' })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '体重记录保存失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteFoodRecord(id: number) {
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteFoodRecord(userId, id)
      await Promise.all([loadDashboard(userId, selectedDate), loadReports(userId)])
      if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '食物记录已删除' })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteExerciseRecord(id: number) {
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteExerciseRecord(userId, id)
      await Promise.all([loadDashboard(userId, selectedDate), loadReports(userId)])
      if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '运动记录已删除' })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  async function deleteWeightRecord(id: number) {
    if (selectedUserId === null) return
    const userId = selectedUserId
    setSaving(true)
    setNotice(null)
    try {
      await api.deleteWeightRecord(userId, id)
      await Promise.all([loadWeightRecords(userId), loadReports(userId), loadProfile(userId)])
      if (selectedUserIdRef.current === userId) setNotice({ type: 'success', message: '体重记录已删除' })
    } catch (error) {
      if (selectedUserIdRef.current === userId) setNotice({ type: 'error', message: getErrorMessage(error, '删除失败') })
    } finally {
      setSaving(false)
    }
  }

  return (
    <AppShell
      activePage={activePage}
      energyTarget={profile?.calorieGoalMode === 'AUTO' ? activePlan?.calculation.baseIntakeTargetCalories ?? null : profile?.dailyCalorieGoal ?? null}
      users={users}
      selectedUserId={selectedUserId}
      loadingUsers={loadingUsers}
      selectedDate={selectedDate}
      notice={notice}
      onSelectedDateChange={handleSelectedDateChange}
      onSelectedUserChange={handleSelectedUserChange}
    >
      {selectedUserId === null ? (
        <section className="panel empty-state">暂无可用用户</section>
      ) : (
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
            path="reports"
            element={
              <ReportsPage weeklyReport={weeklyReport} monthlyReport={monthlyReport} loadingReports={loadingReports} />
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
                activePlan={activePlan}
                planForm={planForm}
                onProfileFormChange={setProfileForm}
                onProfileSubmit={handleProfileSubmit}
                onPlanFormChange={setPlanForm}
                onPlanSubmit={handlePlanSubmit}
              />
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      )}
      {pendingConfirmation && (
        <ConfirmationDialog
          pending={pendingConfirmation}
          saving={saving}
          onConfirm={() => void confirmPending()}
          onCancel={() => setPendingConfirmation(null)}
        />
      )}
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
