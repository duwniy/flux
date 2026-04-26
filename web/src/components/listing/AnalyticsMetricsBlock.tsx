import type { AnalyticsSummary } from '../../types'

interface Props {
  summary: AnalyticsSummary
}

export function AnalyticsMetricsBlock({ summary }: Props) {
  const conversionRate = summary.views > 0 ? ((summary.phoneClicks / summary.views) * 100).toFixed(1) : '0'
  const metrics = [
    {
      label: 'Просмотры',
      value: summary.views.toLocaleString('ru'),
      color: '#7F77DD',
    },
    {
      label: 'Клики на телефон',
      value: summary.phoneClicks.toLocaleString('ru'),
      color: '#534AB7',
    },
    {
      label: 'В избранное',
      value: summary.favorites.toLocaleString('ru'),
      color: '#1D9E75',
    },
    {
      label: 'Конверсия',
      value: `${conversionRate}%`,
      color: '#EF9F27',
    },
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
        Основные метрики аналитики
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12 }}>
        {metrics.map((m) => (
          <div
            key={m.label}
            style={{
              padding: 12,
              background: 'var(--color-background-secondary, #ece9e2)',
              borderRadius: 8,
              borderTop: `3px solid ${m.color}`,
              display: 'flex',
              flexDirection: 'column',
              gap: 6,
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
                fontSize: 20,
                fontWeight: 600,
                color: m.color,
              }}
            >
              {m.value}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
