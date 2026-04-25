import type { AnalyticsSummary } from '../../types'

interface Props {
  totalListings: number
  summary: AnalyticsSummary
}

export function FunnelChart({ totalListings, summary }: Props) {
  const safeTotal = Math.max(totalListings, 1)
  const steps = [
    {
      label: 'Опубликовано',
      value: totalListings,
      color: '#AFA9EC',
      textColor: '#26215C',
    },
    { label: 'Просмотр', value: summary.views, color: '#7F77DD', textColor: '#EEEDFE' },
    {
      label: 'Клик тел.',
      value: summary.phoneClicks,
      color: '#534AB7',
      textColor: '#EEEDFE',
    },
    {
      label: 'Звонок',
      value: Math.round(summary.phoneClicks * 0.26),
      color: '#3C3489',
      textColor: '#EEEDFE',
    },
  ]

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {steps.map((step, i) => {
        const pct = Math.round((step.value / safeTotal) * 100)
        return (
          <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div
              style={{
                fontSize: 12,
                color: 'var(--color-text-secondary, #5f5a52)',
                width: 80,
                textAlign: 'right',
                flexShrink: 0,
              }}
            >
              {step.label}
            </div>
            <div
              style={{
                flex: 1,
                height: 28,
                background: 'var(--color-background-secondary, #ece9e2)',
                borderRadius: 4,
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  width: `${Math.min(Math.max(pct, 0), 100)}%`,
                  height: '100%',
                  background: step.color,
                  borderRadius: 4,
                  display: 'flex',
                  alignItems: 'center',
                  paddingLeft: 10,
                  minWidth: 40,
                }}
              >
                <span style={{ fontSize: 12, fontWeight: 500, color: step.textColor }}>
                  {step.value.toLocaleString('ru')}
                </span>
              </div>
            </div>
            <div
              style={{
                fontSize: 11,
                color: 'var(--color-text-tertiary, #8f877d)',
                width: 36,
                flexShrink: 0,
              }}
            >
              {pct}%
            </div>
          </div>
        )
      })}
    </div>
  )
}
