export type SellerType = 'OWNER' | 'AGENCY' | 'DEVELOPER'
export type EnrichmentStatus = 'PENDING' | 'ENRICHED' | 'FAILED'
export type EventType = 'VIEW' | 'PHONE_CLICK' | 'ADD_TO_FAVORITES'

export interface ScoreFactor {
  factorName: string
  points: number
  maxPoints: number
  recommendation: string | null
}

export interface ListingVersion {
  id: string
  versionNumber: number
  score: number
  price: number
  photosCount: number
  changeReason: string
  validFrom: string
  validTo: string | null
  isCurrent: boolean
}

export interface Listing {
  id: string
  sellerId: string
  title: string
  description: string
  price: number
  totalAreaSqm: number
  districtId: string
  floor: number
  totalFloors: number
  photosCount: number
  sellerType: SellerType
  status: string
  score: number
  scoreBreakdown: ScoreFactor[]
  enrichmentStatus: EnrichmentStatus
  districtMedianPriceSqm: number
  priceDeviationPct: number
  districtDemandIndex: number
  competitorCount: number
  enrichedAt: string
  createdAt: string
}

export interface AnalyticsSummary {
  views: number
  phoneClicks: number
  favorites: number
}

export interface VersionAnalytics {
  versionNumber: number
  score: number
  views: number
  clicks: number
}

export interface DistrictScore {
  districtId: string
  districtName: string
  avgScore: number
  listingCount: number
}

export interface DashboardStats {
  avgScore: number
  avgScoreDelta: number
  activeListings: number
  activeListingsDelta: number
  conversionRate: number
  conversionDelta: number
  enrichedPct: number
  enrichedDelta: number
}

export interface ScoreTrendPoint {
  date: string
  avgScore: number
}

export interface ScatterPoint {
  score: number
  conversionRate: number
  listingId: string
}

export interface Filters {
  city: string
  sellerType: string
  priceSegment: string
  period: string
}

export interface MarketContext {
  districtName: string
  avgPrice: number
  avgPriceSqm: number
  avgScore: number
  demandIndex: number
  competitorCount: number
  avgDaysOnMarket: number
}

export interface ListingRecommendation {
  id: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  title: string
  description: string
  potentialImpact: number
}

export interface ConversionAnalytics {
  date: string
  version: number
  views: number
  clicks: number
  conversionRate: number
}

export interface ScoringModel {
  id: string
  versionNumber: number
  name: string
  factorWeights: Record<string, number>
  isActive: boolean
  activatedAt: string | null
  createdAt: string
}

export interface PipelineHealth {
  name: string
  status: 'UP' | 'DOWN' | 'DEGRADED'
  lastSuccessfulRun: string
  recordsProcessed: number
  errorCount: number
}

export interface HealthReport {
  status: 'UP' | 'DOWN' | 'DEGRADED'
  pipelines: PipelineHealth[]
}

export interface DataQualityCheck {
  checkName: string
  entityType: string
  entityId: string
  passed: boolean
  failureReason: string | null
  checkedAt: string
}

export interface QualityReport {
  totalChecks: number
  passedChecks: number
  failedChecks: number
  successPercentage: number
  failedCheckDetails: DataQualityCheck[]
}

export interface EnrichmentStats {
  totalRuns: number
  successCount: number
  failedCount: number
}

export interface EnrichmentLogEntry {
  id: string
  listingId: string
  status: 'SUCCESS' | 'FAILED'
  errorMsg: string | null
  durationMs: number
  createdAt: string
}
