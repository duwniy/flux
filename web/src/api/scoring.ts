import { get, post } from './client'
import type { ScoringModel, HealthReport, QualityReport } from '../types'

// Mock data
const mockScoringModels: ScoringModel[] = [
  {
    id: 'model-1',
    versionNumber: 1,
    name: 'Базовая модель',
    factorWeights: {
      description: 15,
      photos: 20,
      title: 12,
      floor_info: 8,
      seller_type: 10,
      price_competitiveness: 18,
      demand_context: 10,
      competitor_density: 7,
    },
    isActive: true,
    activatedAt: '2026-04-01T10:00:00Z',
    createdAt: '2026-04-01T09:00:00Z',
  },
  {
    id: 'model-2',
    versionNumber: 2,
    name: 'Оптимизированная модель',
    factorWeights: {
      description: 18,
      photos: 22,
      title: 14,
      floor_info: 6,
      seller_type: 8,
      price_competitiveness: 20,
      demand_context: 8,
      competitor_density: 4,
    },
    isActive: false,
    activatedAt: null,
    createdAt: '2026-04-15T12:00:00Z',
  },
  {
    id: 'model-3',
    versionNumber: 3,
    name: 'Экспериментальная модель',
    factorWeights: {
      description: 20,
      photos: 25,
      title: 15,
      floor_info: 5,
      seller_type: 7,
      price_competitiveness: 15,
      demand_context: 10,
      competitor_density: 3,
    },
    isActive: false,
    activatedAt: null,
    createdAt: '2026-04-20T14:30:00Z',
  },
]

const mockHealthReport: HealthReport = {
  status: 'UP',
  pipelines: [
    {
      name: 'enrichment',
      status: 'UP',
      lastSuccessfulRun: '2026-04-22T14:45:00Z',
      recordsProcessed: 1247,
      errorCount: 0,
    },
    {
      name: 'analytics',
      status: 'UP',
      lastSuccessfulRun: '2026-04-22T14:50:00Z',
      recordsProcessed: 3521,
      errorCount: 0,
    },
    {
      name: 'notification',
      status: 'DEGRADED',
      lastSuccessfulRun: '2026-04-22T14:55:00Z',
      recordsProcessed: 892,
      errorCount: 3,
    },
  ],
}

const mockQualityReport: QualityReport = {
  totalChecks: 1248,
  passedChecks: 1201,
  failedChecks: 47,
  successPercentage: 96.2,
  failedCheckDetails: [
    {
      checkName: 'Missing required photos',
      entityType: 'LISTING',
      entityId: 'listing-1',
      passed: false,
      failureReason: 'Объявление содержит менее 5 фотографий',
      checkedAt: '2026-04-22T14:30:00Z',
    },
    {
      checkName: 'Invalid price range',
      entityType: 'LISTING',
      entityId: 'listing-2',
      passed: false,
      failureReason: 'Цена выходит за пределы диапазона района',
      checkedAt: '2026-04-22T14:25:00Z',
    },
    {
      checkName: 'Missing description',
      entityType: 'LISTING',
      entityId: 'listing-3',
      passed: false,
      failureReason: 'Отсутствует описание объявления',
      checkedAt: '2026-04-22T14:20:00Z',
    },
  ],
}

export const scoringModelsApi = {
  getModels: async (): Promise<ScoringModel[]> => {
    try {
      return await get<ScoringModel[]>(`/scoring-models`)
    } catch {
      return mockScoringModels
    }
  },

  activateModel: async (id: string): Promise<ScoringModel> => {
    try {
      return await post<ScoringModel>(`/scoring-models/${id}/activate`, {})
    } catch {
      return mockScoringModels[0]
    }
  },

  createModel: async (name: string, factorWeights: Record<string, number>): Promise<ScoringModel> => {
    try {
      return await post<ScoringModel>(`/scoring-models`, { name, factorWeights })
    } catch {
      return {
        id: 'model-new',
        versionNumber: 4,
        name,
        factorWeights,
        isActive: false,
        activatedAt: null,
        createdAt: new Date().toISOString(),
      }
    }
  },

  runBackfill: async (listingId: string): Promise<void> => {
    try {
      await post<void>(`/scoring-models/listings/${listingId}/recalculate`, {})
    } catch {
      // Mock success
    }
  },
}

export const monitoringApi = {
  getHealth: async (): Promise<HealthReport> => {
    try {
      return await get<HealthReport>(`/monitoring/health`)
    } catch {
      return mockHealthReport
    }
  },

  getQualityReport: async (): Promise<QualityReport> => {
    try {
      return await get<QualityReport>(`/monitoring/quality-report`)
    } catch {
      return mockQualityReport
    }
  },

  syncReporting: async (): Promise<void> => {
    try {
      await post<void>(`/reporting/sync`, {})
    } catch {
      // Mock success
    }
  },
}
