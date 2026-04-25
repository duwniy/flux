import { get } from './client'
import type { Listing, ListingVersion } from '../types'

export const listingsApi = {
  getById: (id: string) => get<Listing>(`/listings/${id}`),
  getVersions: (id: string) => get<ListingVersion[]>(`/listings/${id}/versions`),
  getContext: (id: string) => get(`/listings/${id}/context`),
  getRecommendations: (id: string) => get(`/listings/${id}/recommendations`),
}
