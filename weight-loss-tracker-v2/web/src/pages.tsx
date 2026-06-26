import type { FormEvent } from 'react'
import type { DailySummary, PeriodReport, RecentSummary, UserProfile, WeightRecord } from './api'
import { ExerciseRecordForm, FoodRecordForm, MetricCard, ProfileGoalForm, RecordListPanel, WeightRecordForm } from './components'
import { goalLabels, mealLabels } from './constants'
import type { ExerciseFormState, FoodFormState, ProfileFormState, WeightFormState } from './types'
import { formatShortDate } from './utils'

export function DashboardPage({
  dailySummary,
  recentSummaries,
  loadingDashboard,
  saving,
  onDeleteFoodRecord,
  onDeleteExerciseRecord,
}: {
  dailySummary: DailySummary | null
  recentSummaries: RecentSummary[]
  loadingDashboard: boolean
  saving: boolean
  onDeleteFoodRecord: (id: number) => void
  onDeleteExerciseRecord: (id: number) => void
}) {
  const trendMax = Math.max(
    1,
    ...recentSummaries.flatMap((item) => [item.totalCaloriesConsumed, item.totalCaloriesBurned, item.dailyCalorieGoal]),
  )
  const rawGoalPercent = dailySummary
    ? Math.round((dailySummary.netCalories / Math.max(1, dailySummary.dailyCalorieGoal)) * 100)
    : 0
  const goalPercent = Math.min(100, Math.max(0, rawGoalPercent))

  return (
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
        <div className={`goal-ring ${dailySummary?.goalStatus.toLowerCase() || 'under'}`}>{goalPercent}%</div>
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
          onDelete: () => onDeleteFoodRecord(record.id),
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
          onDelete: () => onDeleteExerciseRecord(record.id),
        }))}
        loading={loadingDashboard}
        saving={saving}
      />
    </section>
  )
}

export function FoodRecordPage({
  foodForm,
  dailySummary,
  loadingDashboard,
  saving,
  onFoodFormChange,
  onFoodSubmit,
  onDeleteFoodRecord,
}: {
  foodForm: FoodFormState
  dailySummary: DailySummary | null
  loadingDashboard: boolean
  saving: boolean
  onFoodFormChange: (nextForm: FoodFormState) => void
  onFoodSubmit: (event: FormEvent<HTMLFormElement>) => void
  onDeleteFoodRecord: (id: number) => void
}) {
  return (
    <section className="split-page">
      <FoodRecordForm foodForm={foodForm} saving={saving} onSubmit={onFoodSubmit} onChange={onFoodFormChange} />
      <RecordListPanel
        title="当天食物"
        emptyText="当天没有食物记录"
        rows={dailySummary?.foodRecords.map((record) => ({
          id: record.id,
          primary: record.foodName,
          secondary: `${mealLabels[record.mealType]} · 蛋白质 ${record.protein}g`,
          value: `${record.calories} kcal`,
          onDelete: () => onDeleteFoodRecord(record.id),
        }))}
        loading={loadingDashboard}
        saving={saving}
      />
    </section>
  )
}

export function ExerciseRecordPage({
  exerciseForm,
  dailySummary,
  loadingDashboard,
  saving,
  onExerciseFormChange,
  onExerciseSubmit,
  onDeleteExerciseRecord,
}: {
  exerciseForm: ExerciseFormState
  dailySummary: DailySummary | null
  loadingDashboard: boolean
  saving: boolean
  onExerciseFormChange: (nextForm: ExerciseFormState) => void
  onExerciseSubmit: (event: FormEvent<HTMLFormElement>) => void
  onDeleteExerciseRecord: (id: number) => void
}) {
  return (
    <section className="split-page">
      <ExerciseRecordForm exerciseForm={exerciseForm} saving={saving} onSubmit={onExerciseSubmit} onChange={onExerciseFormChange} />
      <RecordListPanel
        title="当天运动"
        emptyText="当天没有运动记录"
        rows={dailySummary?.exerciseRecords.map((record) => ({
          id: record.id,
          primary: record.exerciseName,
          secondary: `${record.exerciseType} · ${record.durationMinutes} 分钟`,
          value: `${record.caloriesBurned} kcal`,
          onDelete: () => onDeleteExerciseRecord(record.id),
        }))}
        loading={loadingDashboard}
        saving={saving}
      />
    </section>
  )
}

export function WeightRecordPage({
  weightForm,
  weightRecords,
  profile,
  loadingWeightRecords,
  saving,
  onWeightFormChange,
  onWeightSubmit,
  onDeleteWeightRecord,
}: {
  weightForm: WeightFormState
  weightRecords: WeightRecord[]
  profile: UserProfile | null
  loadingWeightRecords: boolean
  saving: boolean
  onWeightFormChange: (nextForm: WeightFormState) => void
  onWeightSubmit: (event: FormEvent<HTMLFormElement>) => void
  onDeleteWeightRecord: (id: number) => void
}) {
  const latestWeight = weightRecords.at(-1)?.weightKg
  const firstWeight = weightRecords[0]?.weightKg
  const weightChange = latestWeight !== undefined && firstWeight !== undefined ? Number((latestWeight - firstWeight).toFixed(1)) : undefined
  const targetGap = latestWeight !== undefined && profile ? Number((latestWeight - profile.targetWeightKg).toFixed(1)) : undefined
  const minWeight = Math.min(...weightRecords.map((record) => record.weightKg), profile?.targetWeightKg ?? Number.POSITIVE_INFINITY)
  const maxWeight = Math.max(...weightRecords.map((record) => record.weightKg), profile?.currentWeightKg ?? 0)
  const weightRange = Math.max(1, maxWeight - minWeight)

  return (
    <section className="page-grid weight-page">
      <div className="metric-row">
        <MetricCard label="最新体重" value={latestWeight} suffix="kg" tone="ink" />
        <MetricCard label="目标体重" value={profile?.targetWeightKg} suffix="kg" tone="teal" />
        <MetricCard label="距离目标" value={targetGap} suffix="kg" tone="amber" />
        <MetricCard label="30天变化" value={weightChange} suffix="kg" tone="berry" />
      </div>

      <section className="panel weight-trend-panel">
        <div className="panel-header">
          <div>
            <p className="eyebrow">最近 30 天</p>
            <h3>体重趋势</h3>
          </div>
        </div>
        {loadingWeightRecords ? (
          <div className="empty-state">加载中</div>
        ) : weightRecords.length > 0 ? (
          <div className="weight-trend">
            {weightRecords.map((record) => (
              <div className="weight-point" key={record.id}>
                <span style={{ height: `${20 + ((record.weightKg - minWeight) / weightRange) * 80}%` }}></span>
                <strong>{record.weightKg}kg</strong>
                <small>{formatShortDate(record.recordDate)}</small>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">暂无体重记录</div>
        )}
      </section>

      <section className="split-page weight-entry">
        <WeightRecordForm weightForm={weightForm} saving={saving} onSubmit={onWeightSubmit} onChange={onWeightFormChange} />
        <RecordListPanel
          title="体重记录"
          emptyText="暂无体重记录"
          rows={weightRecords.map((record) => ({
            id: record.id,
            primary: `${record.weightKg} kg`,
            secondary: `${record.recordDate}${record.bodyFatPercentage === null ? '' : ` · 体脂 ${record.bodyFatPercentage}%`}`,
            value: record.note || '记录',
            onDelete: () => onDeleteWeightRecord(record.id),
          }))}
          loading={loadingWeightRecords}
          saving={saving}
        />
      </section>
    </section>
  )
}

export function ReportsPage({
  weeklyReport,
  monthlyReport,
  loadingReports,
}: {
  weeklyReport: PeriodReport | null
  monthlyReport: PeriodReport | null
  loadingReports: boolean
}) {
  return (
    <section className="report-grid">
      <PeriodReportPanel title="7 天周报" report={weeklyReport} loading={loadingReports} />
      <PeriodReportPanel title="30 天月报" report={monthlyReport} loading={loadingReports} />
    </section>
  )
}

function PeriodReportPanel({ title, report, loading }: { title: string; report: PeriodReport | null; loading: boolean }) {
  if (loading) {
    return (
      <section className="panel report-panel">
        <div className="empty-state">加载中</div>
      </section>
    )
  }

  if (!report) {
    return (
      <section className="panel report-panel">
        <div className="empty-state">报表未加载</div>
      </section>
    )
  }

  const trendMax = Math.max(1, ...report.dailySummaries.map((summary) => summary.totalCaloriesConsumed))

  return (
    <section className="panel report-panel">
      <div className="panel-header">
        <div>
          <p className="eyebrow">{report.startDate} 至 {report.endDate}</p>
          <h3>{title}</h3>
        </div>
      </div>
      <div className="report-stats">
        <div><span>总摄入</span><strong>{report.totalCaloriesConsumed}</strong><small>kcal</small></div>
        <div><span>总消耗</span><strong>{report.totalCaloriesBurned}</strong><small>kcal</small></div>
        <div><span>平均净热量</span><strong>{report.averageNetCalories}</strong><small>kcal</small></div>
        <div><span>体重变化</span><strong>{report.weightChangeKg ?? '—'}</strong><small>kg</small></div>
      </div>
      <div className="report-chips">
        <span>低于目标 {report.daysUnderGoal} 天</span>
        <span>接近目标 {report.daysMeetGoal} 天</span>
        <span>超过目标 {report.daysOverGoal} 天</span>
      </div>
      <div className="report-chips">
        <span>蛋白质均值 {report.averageProtein}g</span>
        <span>脂肪均值 {report.averageFat}g</span>
        <span>碳水均值 {report.averageCarbohydrate}g</span>
      </div>
      <div className="report-bars">
        {report.dailySummaries.map((summary) => (
          <div className="report-day" key={summary.date}>
            <span style={{ height: `${Math.max(8, (summary.totalCaloriesConsumed / trendMax) * 100)}%` }}></span>
            <small>{formatShortDate(summary.date)}</small>
          </div>
        ))}
      </div>
    </section>
  )
}

export function ProfilePage({
  profile,
  profileForm,
  loadingProfile,
  saving,
  onProfileFormChange,
  onProfileSubmit,
}: {
  profile: UserProfile | null
  profileForm: ProfileFormState | null
  loadingProfile: boolean
  saving: boolean
  onProfileFormChange: (nextForm: ProfileFormState) => void
  onProfileSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  return (
    <section className="split-page profile-page">
      <ProfileGoalForm
        profileForm={profileForm}
        saving={saving}
        loadingProfile={loadingProfile}
        onSubmit={onProfileSubmit}
        onChange={onProfileFormChange}
      />
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
  )
}
