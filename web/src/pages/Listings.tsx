import { useEffect, useState, useMemo } from 'react'
import { listingsApi } from '../api/listings'
import { ListingsTable } from '../components/listing/ListingsTable'
import type { Listing } from '../types'

export function Listings() {
  const [listings, setListings] = useState<Listing[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [sellerTypeFilter, setSellerTypeFilter] = useState<string>('')
  const [enrichmentStatusFilter, setEnrichmentStatusFilter] = useState<string>('')
  const [scoringFilter, setScoringFilter] = useState<string>('')

  useEffect(() => {
    listingsApi.getAll().then((data) => {
      setListings(data)
      setLoading(false)
    })
  }, [])

  // Filter listings based on all criteria
  const filteredListings = useMemo(() => {
    return listings.filter((listing) => {
      // Search filter (case-insensitive)
      if (
        searchQuery &&
        !listing.title.toLowerCase().includes(searchQuery.toLowerCase())
      ) {
        return false
      }

      // Seller type filter
      if (sellerTypeFilter && listing.sellerType !== sellerTypeFilter) {
        return false
      }

      // Enrichment status filter
      if (
        enrichmentStatusFilter &&
        listing.enrichmentStatus !== enrichmentStatusFilter
      ) {
        return false
      }

      // Scoring filter
      if (scoringFilter) {
        if (scoringFilter === 'LOW' && listing.score >= 40) return false
        if (scoringFilter === 'MEDIUM' && (listing.score < 40 || listing.score >= 70))
          return false
        if (scoringFilter === 'HIGH' && listing.score < 70) return false
      }

      return true
    })
  }, [listings, searchQuery, sellerTypeFilter, enrichmentStatusFilter, scoringFilter])

  if (loading) {
    return (
      <div style={{ padding: 32, color: 'var(--color-text-secondary)' }}>
        Загрузка...
      </div>
    )
  }

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      {/* Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <h2 style={{ fontSize: 18, fontWeight: 500 }}>Объявления</h2>
        <button
          style={{
            background: 'var(--color-primary, #0066ff)',
            color: '#ffffff',
            border: 'none',
            borderRadius: 6,
            padding: '8px 16px',
            fontSize: 13,
            fontWeight: 500,
            cursor: 'pointer',
            transition: 'opacity 200ms',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.opacity = '0.9'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.opacity = '1'
          }}
        >
          Добавить объявление
        </button>
      </div>

      {/* Filters */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(4, 1fr)',
          gap: 12,
        }}
      >
        {/* Search */}
        <input
          type="text"
          placeholder="Поиск по названию"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            padding: '8px 12px',
            fontSize: 13,
            color: 'var(--color-text-primary, #1d1d1b)',
          }}
        />

        {/* Seller Type Filter */}
        <select
          value={sellerTypeFilter}
          onChange={(e) => setSellerTypeFilter(e.target.value)}
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            padding: '8px 12px',
            fontSize: 13,
            color: 'var(--color-text-primary, #1d1d1b)',
            cursor: 'pointer',
          }}
        >
          <option value="">Тип продавца</option>
          <option value="OWNER">Собственник</option>
          <option value="AGENCY">Агентство</option>
          <option value="DEVELOPER">Застройщик</option>
        </select>

        {/* Enrichment Status Filter */}
        <select
          value={enrichmentStatusFilter}
          onChange={(e) => setEnrichmentStatusFilter(e.target.value)}
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            padding: '8px 12px',
            fontSize: 13,
            color: 'var(--color-text-primary, #1d1d1b)',
            cursor: 'pointer',
          }}
        >
          <option value="">Статус обогащения</option>
          <option value="PENDING">В ожидании</option>
          <option value="ENRICHED">Обогащено</option>
          <option value="FAILED">Ошибка</option>
        </select>

        {/* Scoring Filter */}
        <select
          value={scoringFilter}
          onChange={(e) => setScoringFilter(e.target.value)}
          style={{
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            padding: '8px 12px',
            fontSize: 13,
            color: 'var(--color-text-primary, #1d1d1b)',
            cursor: 'pointer',
          }}
        >
          <option value="">Скоринг</option>
          <option value="LOW">Низкий (0-39)</option>
          <option value="MEDIUM">Средний (40-69)</option>
          <option value="HIGH">Высокий (70-100)</option>
        </select>
      </div>

      {/* Table */}
      <div
        style={{
          background: 'var(--color-background-primary, #ffffff)',
          border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
          borderRadius: 12,
          padding: 16,
        }}
      >
        {filteredListings.length > 0 ? (
          <ListingsTable listings={filteredListings} />
        ) : (
          <div
            style={{
              textAlign: 'center',
              padding: '32px 16px',
              color: 'var(--color-text-secondary, #5f5a52)',
            }}
          >
            Объявления не найдены
          </div>
        )}
      </div>

      {/* Info text */}
      <div
        style={{
          fontSize: 12,
          color: 'var(--color-text-tertiary, #8f877d)',
          padding: '0 4px',
        }}
      >
        {filteredListings.length} из {listings.length} объявлений
      </div>
    </div>
  )
}
