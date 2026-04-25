import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { analyticsApi } from '../api/analytics'
import { listingsApi } from '../api/listings'
import { FunnelChart } from '../components/charts/FunnelChart'
import { ListingScoreCard } from '../components/listing/ListingScoreCard'
import type { AnalyticsSummary, Listing, VersionAnalytics } from '../types'

export function ListingDetail() {
  const { id } = useParams<{ id: string }>()
  const [listing, setListing] = useState<Listing | null>(null)
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null)
  const [versions, setVersions] = useState<VersionAnalytics[]>([])

  useEffect(() => {
    if (!id) return
    Promise.all([
      listingsApi.getById(id),
      analyticsApi.getSummary(id),
      analyticsApi.getVersionAnalytics(id),
    ]).then(([l, s, v]) => {
      setListing(l)
      setSummary(s)
      setVersions(v)
    })
  }, [id])

  if (!listing) return <div style={{ padding: 32 }}>Загрузка...</div>

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <h2 style={{ fontSize: 18, fontWeight: 500 }}>{listing.title}</h2>

      <ListingScoreCard listing={listing} />

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

      {versions.length > 0 && (
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
            История версий - скоринг и конверсия
          </div>
          <table style={{ width: '100%', fontSize: 13, borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)' }}>
                {['Версия', 'Скоринг', 'Просмотры', 'Звонки'].map((h) => (
                  <th
                    key={h}
                    style={{
                      padding: '6px 8px',
                      textAlign: 'left',
                      fontWeight: 500,
                      color: 'var(--color-text-secondary, #5f5a52)',
                    }}
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {versions.map((v) => (
                <tr
                  key={v.versionNumber}
                  style={{ borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)' }}
                >
                  <td style={{ padding: '8px 8px' }}>v{v.versionNumber}</td>
                  <td style={{ padding: '8px 8px', fontWeight: 500 }}>{v.score}</td>
                  <td style={{ padding: '8px 8px' }}>{v.views.toLocaleString('ru')}</td>
                  <td style={{ padding: '8px 8px' }}>{v.clicks.toLocaleString('ru')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
