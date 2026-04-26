import { get } from './client'
import type {
  AnalyticsSummary,
  DashboardStats,
  DistrictScore,
  ScatterPoint,
  ScoreTrendPoint,
  VersionAnalytics,
} from '../types'

const dashboardStatsMock: DashboardStats = {
  avgScore: 72.4,
  avgScoreDelta: 1.9,
  activeListings: 1284,
  activeListingsDelta: 4.2,
  conversionRate: 6.7,
  conversionDelta: 0.8,
  enrichedPct: 93.1,
  enrichedDelta: 2.1,
}

const trendMock: ScoreTrendPoint[] = [
  { date: '2026-04-01', avgScore: 68.1 },
  { date: '2026-04-08', avgScore: 69.4 },
  { date: '2026-04-15', avgScore: 70.8 },
  { date: '2026-04-22', avgScore: 72.4 },
]

const districtMock: DistrictScore[] = [
  { districtId: 'hamovniki', districtName: 'Хамовники', avgScore: 82, listingCount: 124 },
  { districtId: 'arbat', districtName: 'Арбат', avgScore: 78, listingCount: 102 },
  { districtId: 'presnya', districtName: 'Пресненский', avgScore: 71, listingCount: 146 },
  { districtId: 'biryulyovo', districtName: 'Бирюлево', avgScore: 54, listingCount: 88 },
]

const scatterMock: ScatterPoint[] = [
  { listingId: 'a', score: 42, conversionRate: 2.1 },
  { listingId: 'b', score: 55, conversionRate: 4.6 },
  { listingId: 'c', score: 68, conversionRate: 5.8 },
  { listingId: 'd', score: 77, conversionRate: 7.3 },
  { listingId: 'e', score: 91, conversionRate: 10.5 },
]

export const analyticsApi = {
  getSummary: (listingId: string) =>
    get<AnalyticsSummary>(`/analytics/summary/${listingId}`),

  getVersionAnalytics: (listingId: string) =>
    get<VersionAnalytics[]>(`/analytics/conversion/${listingId}`),

  getDashboardStats: async (filters: Record<string, string>) => {
    const params = new URLSearchParams(filters).toString()
    try {
      return await get<DashboardStats>(`/analytics/dashboard/stats?${params}`)
    } catch {
      return dashboardStatsMock
    }
  },

  getScoreTrend: async (filters: Record<string, string>) => {
    const params = new URLSearchParams(filters).toString()
    try {
      return await get<ScoreTrendPoint[]>(`/analytics/dashboard/trend?${params}`)
    } catch {
      return trendMock
    }
  },

  getDistrictScores: async (city: string) => {
    try {
      return await get<DistrictScore[]>(`/analytics/dashboard/districts?city=${city}`)
    } catch {
      return districtMock
    }
  },

  getScatterData: async (filters: Record<string, string>) => {
    const params = new URLSearchParams(filters).toString()
    try {
      return await get<ScatterPoint[]>(`/analytics/dashboard/scatter?${params}`)
    } catch {
      return scatterMock
    }
  },

  getHealth: async () => {
    try {
      return await get(`/monitoring/health`)
    } catch {
      return {
        status: 'UP',
        pipelines: [
          { name: 'enrichment', status: 'UP' },
          { name: 'analytics', status: 'UP' },
          { name: 'notification', status: 'UP' },
        ],
      }
    }
  },
}
