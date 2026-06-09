export type PageId = 'dashboard' | 'food' | 'exercise' | 'weight' | 'profile'

export interface PageConfig {
  id: PageId
  label: string
  marker: string
  heading: string
  path: string
}

export const pages: PageConfig[] = [
  { id: 'dashboard', label: '仪表盘', marker: '01', heading: '今日概览', path: '/' },
  { id: 'food', label: '食物记录', marker: '02', heading: '饮食录入', path: '/food' },
  { id: 'exercise', label: '运动记录', marker: '03', heading: '运动录入', path: '/exercise' },
  { id: 'weight', label: '体重趋势', marker: '04', heading: '体重记录', path: '/weight' },
  { id: 'profile', label: '目标资料', marker: '05', heading: '目标设置', path: '/profile' },
]

export function getPageByPath(pathname: string) {
  const normalizedPath = pathname.replace(/\/+$/, '') || '/'
  return pages.find((page) => page.path === normalizedPath)
}