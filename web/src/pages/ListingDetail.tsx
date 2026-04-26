import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { analyticsApi } from '../api/analytics'
import { listingsApi } from '../api/listings'
import { FunnelChart } from '../components/charts/FunnelChart'
import { ConversionTrendChart } from '../components/charts/ConversionTrendChart'
import { ListingScoreCard } from '../components/listing/ListingScoreCard'
import { MarketContextBlock } from '../components/listing/MarketContextBlock'
import { RecommendationsBlock } from '../components/listing/RecommendationsBlock'
import { EnrichmentActionBlock } from '../components/listing/EnrichmentActionBlock'
import { EnrichmentHistoryPanel } from '../components/listing/EnrichmentHistoryPanel'
import { ListingVersionHistory } from '../components/listing/ListingVersionHistory'
import { AnalyticsMetricsBlock } from '../components/listing/AnalyticsMetricsBlock'
import type { AnalyticsSummary, Listing, VersionAnalytics, MarketContext, ListingRecommendation, ConversionAnalytics } from '../types'

export function ListingDetail() {
  const { id } = useParams<{ id: string }>()
  const [listing, setListing] = useState<Listing | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null)
  const [versions, setVersions] = useState<VersionAnalytics[]>([])
  const [marketContext, setMarketContext] = useState<MarketContext | null>(null)
  const [recommendations, setRecommendations] = useState<ListingRecommendation[]>([])
  const [conversionAnalytics, setConversionAnalytics] = useState<ConversionAnalytics[]>([])
  const [listingVersions, setListingVersions] = useState<any[]>([])
  const [historyOpen, setHistoryOpen] = useState(false)

  useEffect(() => {
    if (!id) return
    Promise.all([
      listingsApi.getById(id),
      analyticsApi.getSummary(id),
      analyticsApi.getVersionAnalytics(id),
      listingsApi.getContext(id),
      listingsApi.getRecommendations(id),
      listingsApi.getConversionAnalytics(id),
      listingsApi.getVersions(id),
    ]).then(([l, s, v, ctx, recs, convAn, vers]) => {
      setListing(l)
      setSummary(s)
      setVersions(v)
      setMarketContext(ctx)
      setRecommendations(recs)
      setConversionAnalytics(convAn)
      setListingVersions(vers)
      setError(null)
    }).catch((e) => {
      console.error('Listing details loading failed', e)
      setError('Не удалось загрузить данные объявления')
    })
  }, [id])

  const handleEnrichmentComplete = () => {
    if (id) {
      listingsApi.getById(id).then(setListing)
      listingsApi.getConversionAnalytics(id).then(setConversionAnalytics)
    }
  }

  if (error) return <div style={{ padding: 32, color: '#A32D2D' }}>{error}</div>
  if (!listing) return <div style={{ padding: 32 }}>Загрузка...</div>

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <h2 style={{ fontSize: 18, fontWeight: 500 }}>{listing.title}</h2>
        <button
          onClick={() => setHistoryOpen(true)}
          style={{
            padding: '8px 14px',
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 500,
            cursor: 'pointer',
            color: 'var(--color-text-primary, #1d1d1b)',
            transition: 'background 150ms',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'var(--color-background-secondary, #ece9e2)'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'var(--color-background-primary, #ffffff)'
          }}
        >
          История обогащения
        </button>
      </div>

      {/* Block 1: Score Card */}
      <ListingScoreCard listing={listing} />

      {/* Block 2: Market Context */}
      {marketContext && <MarketContextBlock context={marketContext} />}

      {/* Block 3: Analytics Metrics */}
      {summary && <AnalyticsMetricsBlock summary={summary} />}

      {/* Block 4: Funnel Chart */}
      {summary && (
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
            Воронка по объявлению
          </div>
          <FunnelChart totalListings={1} summary={summary} />
        </div>
      )}

      {/* Block 5: Recommendations */}
      {recommendations.length > 0 && <RecommendationsBlock recommendations={recommendations} />}

      {/* Block 6: Enrichment Action */}
      <EnrichmentActionBlock listing={listing} onEnrichmentComplete={handleEnrichmentComplete} />

      {/* Block 7: Conversion Trend Chart */}
      {conversionAnalytics.length > 0 && <ConversionTrendChart data={conversionAnalytics} />}

      {/* Block 8: Version History */}
      {listingVersions.length > 0 && <ListingVersionHistory versions={listingVersions} />}

      {/* Enrichment History Side Panel */}
      {id && (
        <EnrichmentHistoryPanel
          listingId={id}
          open={historyOpen}
          onClose={() => setHistoryOpen(false)}
        />
      )}
    </div>
  )
}

