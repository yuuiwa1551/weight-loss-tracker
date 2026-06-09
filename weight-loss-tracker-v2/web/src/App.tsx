import { useEffect, useState, type FormEvent } from 'react'
import { api, type DailySummary, type GoalStatus, type MealType, type RecentSummary, type UserProfile } from './api'
import './App.css'

type Page = 'dashboard' | 'food' | 'exercise' | 'profile'

interface FoodFormState {
  recordDate: string
  mealType: MealType
  foodName: string
  calories: string
  protein: string
  fat: string
  carbohydrate: string
  note: string
}

interface ExerciseFormState {
  recordDate: string
  exerciseType: string
  exerciseName: string
  durationMinutes: string
  caloriesBurned: string
  note: string
}

interface ProfileFormState {
  nickname: string
  heightCm: string
  currentWeightKg: string
  targetWeightKg: string
  dailyCalorieGoal: string
}

interface Notice {
  type: 'success' | 'error'
  message: string
}

const pages: Array<{ id: Page; label: string; marker: string }> = [
  { id: 'dashboard', label: '仪表盘', marker: '01' },
  { id: 'food', label: '食物记录', marker: '02' },
  { id: 'exercise', label: '运动记录', marker: '03' },
  { id: 'profile', label: '目标资料', marker: '04' },
]

const mealLabels: Record<MealType, string> = {
  BREAKFAST: '早餐',
  LUNCH: '午餐',
  DINNER: '晚餐',
  SNACK: '加餐',
}

const goalLabels: Record<GoalStatus, string> = {
  UNDER: '低于目标',
  MEET: '接近目标',
  OVER: '超过目标',
}

function today() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

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

function formatShortDate(value: string) {
  return value.slice(5).replace('-', '/')
}

function App() {
  const [page, setPage] = useState<Page>('dashboard')
  const [selectedDate, setSelectedDate] = useState(today())
  const [dailySummary, setDailySummary] = useState<DailySummary | null>(null)
  const [recentSummaries, setRecentSummaries] = useState<RecentSummary[]>([])
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [foodForm, setFoodForm] = useState<FoodFormState>(() => initialFoodForm(selectedDate))
  const [exerciseForm, setExerciseForm] = useState<ExerciseFormState>(() => initialExerciseForm(selectedDate))
  const [profileForm, setProfileForm] = useState<ProfileFormState | null>(null)
  const [loadingDashboard, setLoadingDashboard] = useState(true)
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '仪表盘加载失败' })
    } finally {
      setLoadingDashboard(false)
    }
  }

  function handleSelectedDateChange(nextDate: string) {
    setLoadingDashboard(true)
    setSelectedDate(nextDate)
    setFoodForm((current) => ({ ...current, recordDate: nextDate }))
    setExerciseForm((current) => ({ ...current, recordDate: nextDate }))
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
        setNotice({ type: 'error', message: error instanceof Error ? error.message : '仪表盘加载失败' })
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
        setNotice({ type: 'error', message: error instanceof Error ? error.message : '资料加载失败' })
      } finally {
        if (!ignore) setLoadingProfile(false)
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '食物记录保存失败' })
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '运动记录保存失败' })
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '目标资料更新失败' })
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '删除失败' })
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
      setNotice({ type: 'error', message: error instanceof Error ? error.message : '删除失败' })
    } finally {
      setSaving(false)
    }
  }

  const trendMax = Math.max(
    1,
    ...recentSummaries.flatMap((item) => [item.totalCaloriesConsumed, item.totalCaloriesBurned, item.dailyCalorieGoal]),
  )

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">WT</div>
          <div>
            <p className="eyebrow">Weight Loss Tracker</p>
            <h1>减肥追踪</h1>
          </div>
        </div>
        <nav className="nav-list" aria-label="主导航">
          {pages.map((item) => (
            <button
              key={item.id}
              className={`nav-button ${page === item.id ? 'active' : ''}`}
              type="button"
              onClick={() => setPage(item.id)}
            >
              <span>{item.marker}</span>
              {item.label}
            </button>
          ))}
        </nav>
        <div className="profile-strip">
          <span>目标</span>
          <strong>{profile ? `${profile.dailyCalorieGoal} kcal` : '未加载'}</strong>
        </div>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">{pages.find((item) => item.id === page)?.label}</p>
            <h2>{page === 'dashboard' ? '今日概览' : page === 'food' ? '饮食录入' : page === 'exercise' ? '运动录入' : '目标设置'}</h2>
          </div>
          <label className="date-control">
            <span>日期</span>
            <input type="date" value={selectedDate} onChange={(event) => handleSelectedDateChange(event.target.value)} />
          </label>
        </header>

        {notice && <div className={`notice ${notice.type}`}>{notice.message}</div>}

        {page === 'dashboard' && (
          <section className="page-grid dashboard-grid">
            <div className="metric-row">
              <MetricCard label="摄入" value={dailySummary?.totalCaloriesConsumed} suffix="kcal" tone="ink" />
              <MetricCard label="消耗" value={dailySummary?.totalCaloriesBurned} suffix="kcal" tone="teal" />
              <MetricCard label="净热量" value={dailySummary?.netCalories} suffix="kcal" tone="amber" />
              <MetricCard label="目标差" value={dailySummary?.calorieDifference} suffix="kcal" tone="berry" />
            </div>

            <section className="panel goal-panel">
              <div>
                <p className="eyebrow">目标状态</p>
                <h3>{dailySummary ? goalLabels[dailySummary.goalStatus] : '等待数据'}</h3>
              </div>
              <div className={`goal-ring ${dailySummary?.goalStatus.toLowerCase() || 'under'}`}>
                {dailySummary ? Math.round((dailySummary.netCalories / Math.max(1, dailySummary.dailyCalorieGoal)) * 100) : 0}%
              </div>
              <div className="macro-row">
                <span>蛋白质 {dailySummary?.totalProtein ?? 0}g</span>
                <span>脂肪 {dailySummary?.totalFat ?? 0}g</span>
                <span>碳水 {dailySummary?.totalCarbohydrate ?? 0}g</span>
              </div>
            </section>

            <section className="panel trend-panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">最近 7 天</p>
                  <h3>热量趋势</h3>
                </div>
              </div>
              <div className="trend-chart">
                {recentSummaries.map((item) => (
                  <div className="trend-day" key={item.date}>
                    <div className="trend-bars">
                      <span style={{ height: `${Math.max(6, (item.totalCaloriesConsumed / trendMax) * 100)}%` }}></span>
                      <span style={{ height: `${Math.max(6, (item.totalCaloriesBurned / trendMax) * 100)}%` }}></span>
                    </div>
                    <small>{formatShortDate(item.date)}</small>
                  </div>
                ))}
              </div>
            </section>

            <RecordListPanel
              title="今日食物"
              emptyText="当天没有食物记录"
              rows={dailySummary?.foodRecords.map((record) => ({
                id: record.id,
                primary: record.foodName,
                secondary: mealLabels[record.mealType],
                value: `${record.calories} kcal`,
                onDelete: () => void deleteFoodRecord(record.id),
              }))}
              loading={loadingDashboard}
              saving={saving}
            />

            <RecordListPanel
              title="今日运动"
              emptyText="当天没有运动记录"
              rows={dailySummary?.exerciseRecords.map((record) => ({
                id: record.id,
                primary: record.exerciseName,
                secondary: `${record.exerciseType} · ${record.durationMinutes} 分钟`,
                value: `${record.caloriesBurned} kcal`,
                onDelete: () => void deleteExerciseRecord(record.id),
              }))}
              loading={loadingDashboard}
              saving={saving}
            />
          </section>
        )}

        {page === 'food' && (
          <section className="split-page">
            <form className="panel form-panel" onSubmit={handleFoodSubmit}>
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Food</p>
                  <h3>新增食物</h3>
                </div>
                <button className="primary-button" type="submit" disabled={saving}>＋ 保存</button>
              </div>
              <div className="form-grid">
                <label>日期<input type="date" value={foodForm.recordDate} onChange={(event) => setFoodForm({ ...foodForm, recordDate: event.target.value })} required /></label>
                <label>餐次<select value={foodForm.mealType} onChange={(event) => setFoodForm({ ...foodForm, mealType: event.target.value as MealType })}>{Object.entries(mealLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
                <label className="wide">食物名称<input value={foodForm.foodName} onChange={(event) => setFoodForm({ ...foodForm, foodName: event.target.value })} required maxLength={120} /></label>
                <label>热量<input type="number" min="0" value={foodForm.calories} onChange={(event) => setFoodForm({ ...foodForm, calories: event.target.value })} required /></label>
                <label>蛋白质<input type="number" min="0" step="0.1" value={foodForm.protein} onChange={(event) => setFoodForm({ ...foodForm, protein: event.target.value })} /></label>
                <label>脂肪<input type="number" min="0" step="0.1" value={foodForm.fat} onChange={(event) => setFoodForm({ ...foodForm, fat: event.target.value })} /></label>
                <label>碳水<input type="number" min="0" step="0.1" value={foodForm.carbohydrate} onChange={(event) => setFoodForm({ ...foodForm, carbohydrate: event.target.value })} /></label>
                <label className="wide">备注<textarea value={foodForm.note} onChange={(event) => setFoodForm({ ...foodForm, note: event.target.value })} maxLength={500} /></label>
              </div>
            </form>
            <RecordListPanel
              title="当天食物"
              emptyText="当天没有食物记录"
              rows={dailySummary?.foodRecords.map((record) => ({
                id: record.id,
                primary: record.foodName,
                secondary: `${mealLabels[record.mealType]} · 蛋白质 ${record.protein}g`,
                value: `${record.calories} kcal`,
                onDelete: () => void deleteFoodRecord(record.id),
              }))}
              loading={loadingDashboard}
              saving={saving}
            />
          </section>
        )}

        {page === 'exercise' && (
          <section className="split-page">
            <form className="panel form-panel" onSubmit={handleExerciseSubmit}>
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Exercise</p>
                  <h3>新增运动</h3>
                </div>
                <button className="primary-button" type="submit" disabled={saving}>＋ 保存</button>
              </div>
              <div className="form-grid">
                <label>日期<input type="date" value={exerciseForm.recordDate} onChange={(event) => setExerciseForm({ ...exerciseForm, recordDate: event.target.value })} required /></label>
                <label>类型<input value={exerciseForm.exerciseType} onChange={(event) => setExerciseForm({ ...exerciseForm, exerciseType: event.target.value })} required maxLength={60} /></label>
                <label className="wide">运动名称<input value={exerciseForm.exerciseName} onChange={(event) => setExerciseForm({ ...exerciseForm, exerciseName: event.target.value })} required maxLength={120} /></label>
                <label>时长<input type="number" min="1" max="1440" value={exerciseForm.durationMinutes} onChange={(event) => setExerciseForm({ ...exerciseForm, durationMinutes: event.target.value })} required /></label>
                <label>消耗<input type="number" min="0" value={exerciseForm.caloriesBurned} onChange={(event) => setExerciseForm({ ...exerciseForm, caloriesBurned: event.target.value })} required /></label>
                <label className="wide">备注<textarea value={exerciseForm.note} onChange={(event) => setExerciseForm({ ...exerciseForm, note: event.target.value })} maxLength={500} /></label>
              </div>
            </form>
            <RecordListPanel
              title="当天运动"
              emptyText="当天没有运动记录"
              rows={dailySummary?.exerciseRecords.map((record) => ({
                id: record.id,
                primary: record.exerciseName,
                secondary: `${record.exerciseType} · ${record.durationMinutes} 分钟`,
                value: `${record.caloriesBurned} kcal`,
                onDelete: () => void deleteExerciseRecord(record.id),
              }))}
              loading={loadingDashboard}
              saving={saving}
            />
          </section>
        )}

        {page === 'profile' && (
          <section className="split-page profile-page">
            <form className="panel form-panel" onSubmit={handleProfileSubmit}>
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Profile</p>
                  <h3>基础目标</h3>
                </div>
                <button className="primary-button" type="submit" disabled={saving || loadingProfile}>＋ 更新</button>
              </div>
              {profileForm ? (
                <div className="form-grid">
                  <label className="wide">昵称<input value={profileForm.nickname} onChange={(event) => setProfileForm({ ...profileForm, nickname: event.target.value })} required maxLength={60} /></label>
                  <label>身高<input type="number" min="1" step="0.1" value={profileForm.heightCm} onChange={(event) => setProfileForm({ ...profileForm, heightCm: event.target.value })} required /></label>
                  <label>当前体重<input type="number" min="1" step="0.1" value={profileForm.currentWeightKg} onChange={(event) => setProfileForm({ ...profileForm, currentWeightKg: event.target.value })} required /></label>
                  <label>目标体重<input type="number" min="1" step="0.1" value={profileForm.targetWeightKg} onChange={(event) => setProfileForm({ ...profileForm, targetWeightKg: event.target.value })} required /></label>
                  <label>每日目标<input type="number" min="1" value={profileForm.dailyCalorieGoal} onChange={(event) => setProfileForm({ ...profileForm, dailyCalorieGoal: event.target.value })} required /></label>
                </div>
              ) : (
                <div className="empty-state">资料未加载</div>
              )}
            </form>
            <section className="panel profile-summary">
              <p className="eyebrow">Health</p>
              <h3>{profile?.nickname || '未加载'}</h3>
              <div className="profile-metrics">
                <MetricCard label="BMI" value={profile?.bmi} suffix="" tone="ink" />
                <MetricCard label="待减重" value={profile?.weightToLoseKg} suffix="kg" tone="teal" />
                <MetricCard label="当前体重" value={profile?.currentWeightKg} suffix="kg" tone="amber" />
              </div>
            </section>
          </section>
        )}
      </main>
    </div>
  )
}

function MetricCard({ label, value, suffix, tone }: { label: string; value?: number; suffix: string; tone: 'ink' | 'teal' | 'amber' | 'berry' }) {
  return (
    <section className={`metric-card ${tone}`}>
      <span>{label}</span>
      <strong>{value ?? '—'}</strong>
      <small>{suffix}</small>
    </section>
  )
}

function RecordListPanel({
  title,
  emptyText,
  rows,
  loading,
  saving,
}: {
  title: string
  emptyText: string
  rows?: Array<{ id: number; primary: string; secondary: string; value: string; onDelete: () => void }>
  loading: boolean
  saving: boolean
}) {
  return (
    <section className="panel list-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">Records</p>
          <h3>{title}</h3>
        </div>
        <span className="count-pill">{rows?.length ?? 0}</span>
      </div>
      {loading ? (
        <div className="empty-state">加载中</div>
      ) : rows && rows.length > 0 ? (
        <div className="record-list">
          {rows.map((row) => (
            <article className="record-row" key={row.id}>
              <div>
                <strong>{row.primary}</strong>
                <span>{row.secondary}</span>
              </div>
              <b>{row.value}</b>
              <button type="button" className="icon-button" onClick={row.onDelete} disabled={saving} aria-label="删除记录">×</button>
            </article>
          ))}
        </div>
      ) : (
        <div className="empty-state">{emptyText}</div>
      )}
    </section>
  )
}

export default App
