import { get } from './client'
import type { EnrichmentStats, EnrichmentLogEntry } from '../types'

export const enrichmentApi = {
  getStats: () => get<EnrichmentStats>('/enrichment/stats'),
  getHistory: (listingId: string) =>
    get<EnrichmentLogEntry[]>(`/enrichment/${listingId}/history`),
}
