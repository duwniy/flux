import { create } from 'zustand'
import type { Filters } from '../types'

interface FiltersStore {
  filters: Filters
  setFilter: (key: keyof Filters, value: string) => void
  reset: () => void
}

const defaultFilters: Filters = {
  city: '',
  sellerType: '',
  priceSegment: '',
  period: '30d',
}

export const useFiltersStore = create<FiltersStore>((set) => ({
  filters: defaultFilters,
  setFilter: (key, value) =>
    set((s) => ({ filters: { ...s.filters, [key]: value } })),
  reset: () => set({ filters: defaultFilters }),
}))
