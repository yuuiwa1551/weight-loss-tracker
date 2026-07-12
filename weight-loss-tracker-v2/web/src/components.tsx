import type { FormEvent, ReactNode } from 'react'
import { NavLink } from 'react-router-dom'
import type { AppUser, EnergyBudget, EnergyPlan, MealType } from './api'
import { activityLabels, calorieGoalModeLabels, formulaSexLabels, mealLabels } from './constants'
import { pages, type PageConfig } from './routes'
import type {
  EnergyPlanFormState,
  ExerciseFormState,
  FoodFormState,
  Notice,
  PendingConfirmation,
  ProfileFormState,
  RecordListRow,
  WeightFormState,
} from './types'

interface AppShellProps {
  activePage: PageConfig
  energyTarget: number | null
  users: AppUser[]
  selectedUserId: number | null
  loadingUsers: boolean
  selectedDate: string
  notice: Notice | null
  onSelectedDateChange: (nextDate: string) => void
  onSelectedUserChange: (userId: number) => void
  children: ReactNode
}

export function AppShell({
  activePage,
  energyTarget,
  users,
  selectedUserId,
  loadingUsers,
  selectedDate,
  notice,
  onSelectedDateChange,
  onSelectedUserChange,
  children,
}: AppShellProps) {
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
            <NavLink
              key={item.id}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) => `nav-button ${isActive ? 'active' : ''}`}
            >
              <span>{item.marker}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="profile-strip">
          <span>目标</span>
          <strong>{energyTarget != null ? `${energyTarget} kcal` : '未设置'}</strong>
        </div>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">{activePage.label}</p>
            <h2>{activePage.heading}</h2>
          </div>
          <div className="topbar-controls">
            <label className="user-control">
              <span>用户</span>
              <select
                aria-label="当前用户"
                value={selectedUserId ?? ''}
                disabled={loadingUsers || users.length === 0}
                onChange={(event) => onSelectedUserChange(Number(event.target.value))}
              >
                {users.length === 0 && <option value="">暂无用户</option>}
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.displayName || user.username} / {user.username}
                  </option>
                ))}
              </select>
            </label>
            <label className="date-control">
              <span>日期</span>
              <input type="date" value={selectedDate} onChange={(event) => onSelectedDateChange(event.target.value)} />
            </label>
          </div>
        </header>

        {notice && <NoticeBanner notice={notice} />}
        {children}
      </main>
    </div>
  )
}

export function NoticeBanner({ notice }: { notice: Notice }) {
  return <div className={`notice ${notice.type}`}>{notice.message}</div>
}

export function MetricCard({
  label,
  value,
  suffix,
  tone,
}: {
  label: string
  value?: number | null
  suffix: string
  tone: 'ink' | 'teal' | 'amber' | 'berry'
}) {
  return (
    <section className={`metric-card ${tone}`}>
      <span>{label}</span>
      <strong>{value ?? '—'}</strong>
      <small>{suffix}</small>
    </section>
  )
}

export function RecordListPanel({
  title,
  emptyText,
  rows,
  loading,
  saving,
}: {
  title: string
  emptyText: string
  rows?: RecordListRow[]
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
              <button type="button" className="icon-button" onClick={row.onDelete} disabled={saving} aria-label="删除记录">
                ×
              </button>
            </article>
          ))}
        </div>
      ) : (
        <div className="empty-state">{emptyText}</div>
      )}
    </section>
  )
}

export function FoodRecordForm({
  foodForm,
  saving,
  onSubmit,
  onChange,
}: {
  foodForm: FoodFormState
  saving: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: (nextForm: FoodFormState) => void
}) {
  return (
    <form className="panel form-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <div>
          <p className="eyebrow">Food</p>
          <h3>新增食物</h3>
        </div>
        <button className="primary-button" type="submit" disabled={saving}>预览</button>
      </div>
      <div className="form-grid">
        <label>日期<input type="date" value={foodForm.recordDate} onChange={(event) => onChange({ ...foodForm, recordDate: event.target.value })} required /></label>
        <label>餐次<select value={foodForm.mealType} onChange={(event) => onChange({ ...foodForm, mealType: event.target.value as MealType })}>{Object.entries(mealLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
        <label className="wide">食物名称<input value={foodForm.foodName} onChange={(event) => onChange({ ...foodForm, foodName: event.target.value })} required maxLength={120} /></label>
        <label>热量<input type="number" min="0" value={foodForm.calories} onChange={(event) => onChange({ ...foodForm, calories: event.target.value })} required /></label>
        <label>蛋白质<input type="number" min="0" step="0.1" value={foodForm.protein} onChange={(event) => onChange({ ...foodForm, protein: event.target.value })} /></label>
        <label>脂肪<input type="number" min="0" step="0.1" value={foodForm.fat} onChange={(event) => onChange({ ...foodForm, fat: event.target.value })} /></label>
        <label>碳水<input type="number" min="0" step="0.1" value={foodForm.carbohydrate} onChange={(event) => onChange({ ...foodForm, carbohydrate: event.target.value })} /></label>
        <label className="wide">备注<textarea value={foodForm.note} onChange={(event) => onChange({ ...foodForm, note: event.target.value })} maxLength={500} /></label>
      </div>
    </form>
  )
}

export function ExerciseRecordForm({
  exerciseForm,
  saving,
  onSubmit,
  onChange,
}: {
  exerciseForm: ExerciseFormState
  saving: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: (nextForm: ExerciseFormState) => void
}) {
  return (
    <form className="panel form-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <div>
          <p className="eyebrow">Exercise</p>
          <h3>新增运动</h3>
        </div>
        <button className="primary-button" type="submit" disabled={saving}>预览</button>
      </div>
      <div className="form-grid">
        <label>日期<input type="date" value={exerciseForm.recordDate} onChange={(event) => onChange({ ...exerciseForm, recordDate: event.target.value })} required /></label>
        <label>类型<input value={exerciseForm.exerciseType} onChange={(event) => onChange({ ...exerciseForm, exerciseType: event.target.value })} required maxLength={60} /></label>
        <label className="wide">运动名称<input value={exerciseForm.exerciseName} onChange={(event) => onChange({ ...exerciseForm, exerciseName: event.target.value })} required maxLength={120} /></label>
        <label>时长<input type="number" min="1" max="1440" value={exerciseForm.durationMinutes} onChange={(event) => onChange({ ...exerciseForm, durationMinutes: event.target.value })} required /></label>
        <label>消耗<input type="number" min="0" value={exerciseForm.caloriesBurned} onChange={(event) => onChange({ ...exerciseForm, caloriesBurned: event.target.value })} required /></label>
        <label className="wide">备注<textarea value={exerciseForm.note} onChange={(event) => onChange({ ...exerciseForm, note: event.target.value })} maxLength={500} /></label>
      </div>
    </form>
  )
}

export function WeightRecordForm({
  weightForm,
  saving,
  onSubmit,
  onChange,
}: {
  weightForm: WeightFormState
  saving: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: (nextForm: WeightFormState) => void
}) {
  return (
    <form className="panel form-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <div>
          <p className="eyebrow">Weight</p>
          <h3>新增体重</h3>
        </div>
        <button className="primary-button" type="submit" disabled={saving}>＋ 保存</button>
      </div>
      <div className="form-grid">
        <label>日期<input type="date" value={weightForm.recordDate} onChange={(event) => onChange({ ...weightForm, recordDate: event.target.value })} required /></label>
        <label>体重<input type="number" min="1" step="0.1" value={weightForm.weightKg} onChange={(event) => onChange({ ...weightForm, weightKg: event.target.value })} required /></label>
        <label>体脂率<input type="number" min="0" max="100" step="0.1" value={weightForm.bodyFatPercentage} onChange={(event) => onChange({ ...weightForm, bodyFatPercentage: event.target.value })} /></label>
        <label className="wide">备注<textarea value={weightForm.note} onChange={(event) => onChange({ ...weightForm, note: event.target.value })} maxLength={500} /></label>
      </div>
    </form>
  )
}

export function ProfileGoalForm({
  profileForm,
  saving,
  loadingProfile,
  onSubmit,
  onChange,
}: {
  profileForm: ProfileFormState | null
  saving: boolean
  loadingProfile: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: (nextForm: ProfileFormState) => void
}) {
  return (
    <form className="panel form-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <div>
          <p className="eyebrow">Profile</p>
          <h3>基础目标</h3>
        </div>
        <button className="primary-button" type="submit" disabled={saving || loadingProfile}>更新</button>
      </div>
      {profileForm ? (
        <div className="form-grid">
          <label className="wide">昵称<input value={profileForm.nickname} onChange={(event) => onChange({ ...profileForm, nickname: event.target.value })} maxLength={60} /></label>
          <label>身高<input type="number" min="50" max="250" step="0.1" value={profileForm.heightCm} onChange={(event) => onChange({ ...profileForm, heightCm: event.target.value })} /></label>
          <label>当前体重<input type="number" min="20" max="500" step="0.1" value={profileForm.currentWeightKg} onChange={(event) => onChange({ ...profileForm, currentWeightKg: event.target.value })} /></label>
          <label>目标体重<input type="number" min="20" max="500" step="0.1" value={profileForm.targetWeightKg} onChange={(event) => onChange({ ...profileForm, targetWeightKg: event.target.value })} /></label>
          <label>年龄<input type="number" min="1" value={profileForm.ageYears} onChange={(event) => onChange({ ...profileForm, ageYears: event.target.value })} /></label>
          <label>计算用性别<select value={profileForm.formulaSex} onChange={(event) => onChange({ ...profileForm, formulaSex: event.target.value as ProfileFormState['formulaSex'] })}><option value="">未设置</option>{Object.entries(formulaSexLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label>日常活动<select value={profileForm.nonExerciseActivityLevel} onChange={(event) => onChange({ ...profileForm, nonExerciseActivityLevel: event.target.value as ProfileFormState['nonExerciseActivityLevel'] })}><option value="">未设置</option>{Object.entries(activityLabels).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
          <label>目标模式<select value={profileForm.calorieGoalMode} onChange={(event) => onChange({ ...profileForm, calorieGoalMode: event.target.value as ProfileFormState['calorieGoalMode'] })}>{Object.entries(calorieGoalModeLabels).map(([value, label]) => <option key={value} value={value} disabled={value === 'AUTO' && profileForm.calorieGoalMode !== 'AUTO'}>{label}</option>)}</select></label>
          <label className="wide">手动每日目标<input type="number" min="500" max="10000" value={profileForm.dailyCalorieGoal} onChange={(event) => onChange({ ...profileForm, dailyCalorieGoal: event.target.value })} disabled={profileForm.calorieGoalMode !== 'MANUAL'} required={profileForm.calorieGoalMode === 'MANUAL'} /></label>
        </div>
      ) : (
        <div className="empty-state">资料未加载</div>
      )}
    </form>
  )
}

export function EnergyPlanForm({
  planForm,
  activePlan,
  energyProfileComplete,
  saving,
  onSubmit,
  onChange,
}: {
  planForm: EnergyPlanFormState
  activePlan: EnergyPlan | null
  energyProfileComplete: boolean
  saving: boolean
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onChange: (nextForm: EnergyPlanFormState) => void
}) {
  const calculation = activePlan?.calculation
  return (
    <form className="panel form-panel plan-panel" onSubmit={onSubmit}>
      <div className="panel-header">
        <div>
          <p className="eyebrow">Energy plan</p>
          <h3>能量计划</h3>
        </div>
        <button className="primary-button" type="submit" disabled={saving || !energyProfileComplete}>预览</button>
      </div>
      <div className="segmented-control" aria-label="缺口计算方式">
        {([
          ['DEFAULT_RATE', '默认比例'],
          ['EXPLICIT', '每日缺口'],
          ['TARGET_PERIOD', '目标周期'],
        ] as const).map(([value, label]) => (
          <button
            key={value}
            type="button"
            className={planForm.mode === value ? 'active' : ''}
            onClick={() => onChange({ ...planForm, mode: value })}
          >
            {label}
          </button>
        ))}
      </div>
      <div className="form-grid plan-inputs">
        {planForm.mode === 'EXPLICIT' && (
          <label className="wide">每日缺口<input type="number" min="0" value={planForm.dailyDeficitCalories} onChange={(event) => onChange({ ...planForm, dailyDeficitCalories: event.target.value })} required /></label>
        )}
        {planForm.mode === 'TARGET_PERIOD' && (
          <label className="wide">目标周期<input type="number" min="1" value={planForm.targetPeriodDays} onChange={(event) => onChange({ ...planForm, targetPeriodDays: event.target.value })} required /></label>
        )}
      </div>
      <div className="active-plan-strip">
        <span>当前计划</span>
        <strong>{calculation ? `${calculation.baseIntakeTargetCalories} kcal` : '未启用'}</strong>
        <small>{calculation ? `每日缺口 ${calculation.dailyDeficitCalories} kcal` : '—'}</small>
      </div>
    </form>
  )
}

function BudgetPreview({ budget }: { budget: EnergyBudget }) {
  return (
    <div className="confirmation-values budget-values">
      <div><span>累计摄入</span><strong>{budget.caloriesConsumed} kcal</strong></div>
      <div><span>运动消耗</span><strong>{budget.exerciseCaloriesBurned} kcal</strong></div>
      <div><span>当日预算</span><strong>{budget.todayIntakeBudgetCalories ?? '—'} kcal</strong></div>
      <div><span>剩余可摄入</span><strong>{budget.remainingIntakeCalories ?? '—'} kcal</strong></div>
      <div><span>预计总消耗</span><strong>{budget.estimatedTotalExpenditureCalories ?? '—'} kcal</strong></div>
      <div><span>当前预计缺口</span><strong>{budget.projectedDeficitCalories ?? '—'} kcal</strong></div>
    </div>
  )
}

export function ConfirmationDialog({
  pending,
  saving,
  onConfirm,
  onCancel,
}: {
  pending: PendingConfirmation
  saving: boolean
  onConfirm: () => void
  onCancel: () => void
}) {
  const title = pending.kind === 'food' ? '确认食物记录' : pending.kind === 'exercise' ? '确认运动记录' : '确认能量计划'
  return (
    <div className="dialog-backdrop" role="presentation">
      <section className="confirmation-dialog" role="dialog" aria-modal="true" aria-labelledby="confirmation-title">
        <div className="dialog-header">
          <div>
            <p className="eyebrow">Confirm</p>
            <h3 id="confirmation-title">{title}</h3>
          </div>
          <button type="button" className="close-button" onClick={onCancel} disabled={saving} aria-label="关闭确认框">×</button>
        </div>
        {pending.kind === 'food' && (
          <>
            <div className="confirmation-values">
              <div><span>食物</span><strong>{pending.preview.foodName}</strong></div>
              <div><span>热量</span><strong>{pending.preview.calories} kcal</strong></div>
              <div><span>蛋白质</span><strong>{pending.preview.protein} g</strong></div>
              <div><span>脂肪</span><strong>{pending.preview.fat} g</strong></div>
              <div><span>碳水</span><strong>{pending.preview.carbohydrate} g</strong></div>
            </div>
            <BudgetPreview budget={pending.preview.projectedEnergyBudget} />
          </>
        )}
        {pending.kind === 'exercise' && (
          <>
            <div className="confirmation-values">
              <div><span>运动</span><strong>{pending.preview.exerciseName}</strong></div>
              <div><span>类型</span><strong>{pending.preview.exerciseType}</strong></div>
              <div><span>时长</span><strong>{pending.preview.durationMinutes} 分钟</strong></div>
              <div><span>消耗</span><strong>{pending.preview.caloriesBurned} kcal</strong></div>
            </div>
            <BudgetPreview budget={pending.preview.projectedEnergyBudget} />
          </>
        )}
        {pending.kind === 'energyPlan' && (
          <div className="confirmation-values plan-values">
            <div><span>静息消耗</span><strong>{pending.preview.calculation.restingEnergyCalories} kcal</strong></div>
            <div><span>基础日常消耗</span><strong>{pending.preview.calculation.baselineExpenditureCalories} kcal</strong></div>
            <div><span>每日缺口</span><strong>{pending.preview.calculation.dailyDeficitCalories} kcal</strong></div>
            <div><span>基础摄入预算</span><strong>{pending.preview.calculation.baseIntakeTargetCalories} kcal</strong></div>
            <div><span>当前体重</span><strong>{pending.preview.calculation.weightKg} kg</strong></div>
            <div><span>目标体重</span><strong>{pending.preview.calculation.targetWeightKg ?? '—'} kg</strong></div>
          </div>
        )}
        <div className="dialog-actions">
          <button type="button" className="secondary-button" onClick={onCancel} disabled={saving}>取消</button>
          <button type="button" className="primary-button" onClick={onConfirm} disabled={saving}>{saving ? '提交中' : '确认写入'}</button>
        </div>
      </section>
    </div>
  )
}
