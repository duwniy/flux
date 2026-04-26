import type { MarketContext } from '../../types'

interface Props {
  context: MarketContext
}

export function MarketContextBlock({ context }: Props) {
  const metrics = [
    { label: 'Средняя цена в районе', value: `${(context.avgPrice / 1000000).toFixed(1)}M`, subtext: 'руб.' },
    { label: 'Цена за кв.м', value: `${context.avgPriceSqm.toLocaleString('ru')}`, subtext: 'руб/кв.м' },
    { label: 'Средний скоринг', value: context.avgScore, subtext: 'из 100' },
    { label: 'Индекс спроса', value: context.demandIndex, subtext: '' },
    { label: 'Конкурентов в районе', value: context.competitorCount, subtext: '' },
    { label: 'Дней на рынке', value: context.avgDaysOnMarket, subtext: 'в среднем' },
  ]

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
        Контекст рынка - {context.districtName}
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 12 }}>
        {metrics.map((m) => (
          <div
            key={m.label}
            style={{
              padding: 12,
              background: 'var(--color-background-secondary, #ece9e2)',
              borderRadius: 8,
              display: 'flex',
              flexDirection: 'column',
              gap: 4,
            }}
          >
            <div
              style={{
                fontSize: 11,
                color: 'var(--color-text-tertiary, #8f877d)',
              }}
            >
              {m.label}
            </div>
            <div
              style={{
                fontSize: 16,
                fontWeight: 600,
                color: 'var(--color-text-primary, #1d1d1b)',
              }}
            >
              {m.value}
            </div>
            {m.subtext && (
              <div
                style={{
                  fontSize: 10,
                  color: 'var(--color-text-tertiary, #8f877d)',
                }}
              >
                {m.subtext}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
