import { useState } from 'react'
import type { Listing } from '../../types'

interface Props {
  listing: Listing
  onEnrichmentComplete?: () => void
}

export function EnrichmentActionBlock({ listing, onEnrichmentComplete }: Props) {
  const [isEnriching, setIsEnriching] = useState(false)

  const handleEnrich = async () => {
    if (listing.enrichmentStatus !== 'PENDING') return
    setIsEnriching(true)
    // Simulate enrichment API call
    await new Promise((resolve) => setTimeout(resolve, 2000))
    setIsEnriching(false)
    onEnrichmentComplete?.()
  }

  const statusConfig: Record<string, { label: string; color: string; bg: string }> = {
    PENDING: { label: 'Ожидает обогащения', color: '#EF9F27', bg: '#FFFBF0' },
    ENRICHED: { label: 'Обогащено', color: '#1D9E75', bg: '#F0FDF4' },
    FAILED: { label: 'Ошибка обогащения', color: '#D1293D', bg: '#FEF3F2' },
  }

  const status = statusConfig[listing.enrichmentStatus]

  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        padding: 16,
      }}
    >
      <div
        style={{
          fontSize: 13,
          fontWeight: 500,
          color: 'var(--color-text-secondary, #5f5a52)',
          marginBottom: 14,
        }}
      >
        Статус обогащения данных
      </div>
      <div
        style={{
          padding: 12,
          background: status.bg,
          borderRadius: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: 12,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div
            style={{
              width: 8,
              height: 8,
              borderRadius: '50%',
              background: status.color,
              flexShrink: 0,
            }}
          />
          <div>
            <div
              style={{
                fontSize: 13,
                fontWeight: 500,
                color: status.color,
              }}
            >
              {status.label}
            </div>
            {listing.enrichmentStatus === 'ENRICHED' && (
              <div
                style={{
                  fontSize: 11,
                  color: 'var(--color-text-tertiary, #8f877d)',
                  marginTop: 2,
                }}
              >
                Обогащено {new Date(listing.enrichedAt).toLocaleDateString('ru-RU')}
              </div>
            )}
          </div>
        </div>
        {listing.enrichmentStatus === 'PENDING' && (
          <button
            onClick={handleEnrich}
            disabled={isEnriching}
            style={{
              padding: '6px 12px',
              background: '#534AB7',
              color: '#ffffff',
              border: 'none',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              cursor: isEnriching ? 'not-allowed' : 'pointer',
              opacity: isEnriching ? 0.6 : 1,
            }}
          >
            {isEnriching ? 'Обогащение...' : 'Обогатить'}
          </button>
        )}
      </div>
    </div>
  )
}
