import type { Listing } from '../../types'

interface Props {
  listing: Listing
}

const factorLabel: Record<string, string> = {
  description: 'Описание',
  photos: 'Фотографии',
  title: 'Заголовок',
  floor_info: 'Этаж/этажность',
  seller_type: 'Тип продавца',
  price_competitiveness: 'Цена vs рынок',
  demand_context: 'Спрос района',
  competitor_density: 'Конкуренция',
}

const barColor = (pts: number, max: number) => {
  const ratio = pts / max
  if (ratio >= 0.8) return '#1D9E75'
  if (ratio >= 0.5) return '#EF9F27'
  return '#E24B4A'
}

export function ListingScoreCard({ listing }: Props) {
  const factors = listing.scoreBreakdown ?? []
  const recs = factors.filter((f) => f.recommendation)

  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        padding: 16,
      }}
    >
      <div style={{ display: 'flex', gap: 20 }}>
        <div style={{ flexShrink: 0, width: 100 }}>
          <div
            style={{
              width: 80,
              height: 80,
              borderRadius: '50%',
              border: `4px solid ${barColor(listing.score, 100)}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexDirection: 'column',
              margin: '0 auto',
            }}
          >
            <span style={{ fontSize: 24, fontWeight: 500 }}>{listing.score}</span>
            <span style={{ fontSize: 11, color: 'var(--color-text-secondary, #5f5a52)' }}>
              из 100
            </span>
          </div>

          <div style={{ marginTop: 12 }}>
            <div
              style={{
                fontSize: 11,
                color: 'var(--color-text-tertiary, #8f877d)',
                marginBottom: 4,
              }}
            >
              Отклонение цены
            </div>
            <div
              style={{
                fontSize: 14,
                fontWeight: 500,
                color:
                  listing.priceDeviationPct > 10
                    ? '#A32D2D'
                    : listing.priceDeviationPct < -5
                      ? '#3B6D11'
                      : 'var(--color-text-primary, #1d1d1b)',
              }}
            >
              {listing.priceDeviationPct > 0 ? '+' : ''}
              {listing.priceDeviationPct.toFixed(1)}%
            </div>
            <div
              style={{
                fontSize: 11,
                color: 'var(--color-text-tertiary, #8f877d)',
                marginTop: 2,
              }}
            >
              vs медиана района
            </div>
          </div>
        </div>

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 7 }}>
          {factors.map((f) => (
            <div key={f.factorName} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <div
                style={{
                  width: 110,
                  fontSize: 12,
                  color: 'var(--color-text-secondary, #5f5a52)',
                  flexShrink: 0,
                }}
              >
                {factorLabel[f.factorName] ?? f.factorName}
              </div>
              <div
                style={{
                  flex: 1,
                  height: 6,
                  background: 'var(--color-background-secondary, #ece9e2)',
                  borderRadius: 3,
                  overflow: 'hidden',
                }}
              >
                <div
                  style={{
                    width: `${(f.points / f.maxPoints) * 100}%`,
                    height: '100%',
                    borderRadius: 3,
                    background: barColor(f.points, f.maxPoints),
                  }}
                />
              </div>
              <div
                style={{
                  fontSize: 12,
                  width: 40,
                  textAlign: 'right',
                  color: 'var(--color-text-primary, #1d1d1b)',
                }}
              >
                {f.points}/{f.maxPoints}
              </div>
            </div>
          ))}

          {recs.length > 0 && (
            <div
              style={{
                marginTop: 8,
                padding: '8px 12px',
                background: '#FAEEDA',
                borderRadius: 6,
                fontSize: 12,
                color: '#633806',
              }}
            >
              {recs[0].recommendation}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
