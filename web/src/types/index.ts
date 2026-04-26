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
