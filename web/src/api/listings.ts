import { get } from './client'
import type { Listing, ListingVersion, MarketContext, ListingRecommendation, ConversionAnalytics } from '../types'

// TODO: Replace with GET /api/v1/listings when endpoint is available
const listingsMock: Listing[] = [
  {
    id: 'listing-1',
    sellerId: 'seller-1',
    title: 'Квартира на ул. Пушкина',
    description: 'Просторная квартира в центре города',
    price: 45000000,
    totalAreaSqm: 120,
    districtId: 'hamovniki',
    floor: 5,
    totalFloors: 9,
    photosCount: 12,
    sellerType: 'OWNER',
    status: 'ACTIVE',
    score: 85,
    scoreBreakdown: [],
    enrichmentStatus: 'ENRICHED',
    districtMedianPriceSqm: 375000,
    priceDeviationPct: 2.1,
    districtDemandIndex: 8.5,
    competitorCount: 42,
    enrichedAt: '2026-04-20T10:30:00Z',
    createdAt: '2026-04-18T14:22:00Z',
  },
  {
    id: 'listing-2',
    sellerId: 'seller-2',
    title: 'Апартаменты на Арбате',
    description: 'Элегантные апартаменты с видом на улицу',
    price: 52000000,
    totalAreaSqm: 95,
    districtId: 'arbat',
    floor: 3,
    totalFloors: 7,
    photosCount: 18,
    sellerType: 'AGENCY',
    status: 'ACTIVE',
    score: 78,
    scoreBreakdown: [],
    enrichmentStatus: 'ENRICHED',
    districtMedianPriceSqm: 420000,
    priceDeviationPct: -1.5,
    districtDemandIndex: 7.2,
    competitorCount: 67,
    enrichedAt: '2026-04-19T09:15:00Z',
    createdAt: '2026-04-15T16:45:00Z',
  },
  {
    id: 'listing-3',
    sellerId: 'seller-3',
    title: 'Новая квартира в Пресненском',
    description: 'Современная новостройка',
    price: 38000000,
    totalAreaSqm: 110,
    districtId: 'presnya',
    floor: 8,
    totalFloors: 12,
    photosCount: 20,
    sellerType: 'DEVELOPER',
    status: 'ACTIVE',
    score: 71,
    scoreBreakdown: [],
    enrichmentStatus: 'ENRICHED',
    districtMedianPriceSqm: 350000,
    priceDeviationPct: 0.8,
    districtDemandIndex: 6.8,
    competitorCount: 51,
    enrichedAt: '2026-04-21T11:20:00Z',
    createdAt: '2026-04-16T13:10:00Z',
  },
  {
    id: 'listing-4',
    sellerId: 'seller-4',
    title: 'Квартира в Бирюлево',
    description: 'Доступный вариант для семьи',
    price: 22000000,
    totalAreaSqm: 85,
    districtId: 'biryulyovo',
    floor: 4,
    totalFloors: 5,
    photosCount: 8,
    sellerType: 'OWNER',
    status: 'ACTIVE',
    score: 54,
    scoreBreakdown: [],
    enrichmentStatus: 'PENDING',
    districtMedianPriceSqm: 260000,
    priceDeviationPct: 3.2,
    districtDemandIndex: 4.1,
    competitorCount: 34,
    enrichedAt: null as unknown as string,
    createdAt: '2026-04-22T10:00:00Z',
  },
]

export const listingsApi = {
  getById: (id: string) => get<Listing>(`/listings/${id}`),
  getVersions: (id: string) => get<ListingVersion[]>(`/listings/${id}/versions`),
  getContext: (id: string) => getMarketContext(id),
  getRecommendations: (id: string) => getRecommendations(id),
  getConversionAnalytics: (id: string) => getConversionAnalytics(id),

  getAll: async () => {
    try {
      return await get<Listing[]>(`/listings`)
    } catch {
      return listingsMock
    }
  },
}

// Mock data functions for the 6 blocks
async function getMarketContext(id: string): Promise<MarketContext> {
  return {
    districtName: 'Хамовники',
    avgPrice: 45000000,
    avgPriceSqm: 375000,
    avgScore: 75,
    demandIndex: 8.5,
    competitorCount: 42,
    avgDaysOnMarket: 45,
  }
}

async function getRecommendations(id: string): Promise<ListingRecommendation[]> {
  return [
    {
      id: 'rec-1',
      priority: 'HIGH',
      title: 'Добавьте больше фотографий',
      description: 'Объявления с 15+ фотографиями получают на 35% больше просмотров',
      potentialImpact: 35,
    },
    {
      id: 'rec-2',
      priority: 'HIGH',
      title: 'Улучшите заголовок',
      description: 'Используйте более привлекательные ключевые слова в заголовке',
      potentialImpact: 28,
    },
    {
      id: 'rec-3',
      priority: 'MEDIUM',
      title: 'Добавьте описание преимуществ',
      description: 'Объявления с подробным описанием имеют выше rate конверсии',
      potentialImpact: 18,
    },
  ]
}

async function getConversionAnalytics(id: string): Promise<ConversionAnalytics[]> {
  return [
    { date: '2026-04-15', version: 1, views: 120, clicks: 8, conversionRate: 6.7 },
    { date: '2026-04-16', version: 1, views: 145, clicks: 10, conversionRate: 6.9 },
    { date: '2026-04-17', version: 1, views: 132, clicks: 9, conversionRate: 6.8 },
    { date: '2026-04-18', version: 2, views: 165, clicks: 14, conversionRate: 8.5 },
    { date: '2026-04-19', version: 2, views: 178, clicks: 16, conversionRate: 9.0 },
    { date: '2026-04-20', version: 2, views: 192, clicks: 18, conversionRate: 9.4 },
    { date: '2026-04-21', version: 2, views: 210, clicks: 20, conversionRate: 9.5 },
    { date: '2026-04-22', version: 3, views: 235, clicks: 24, conversionRate: 10.2 },
  ]
}
