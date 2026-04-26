import type { ListingRecommendation } from '../../types'

interface Props {
  recommendations: ListingRecommendation[]
}

const priorityColors: Record<string, { bg: string; badge: string; text: string }> = {
  HIGH: { bg: '#FEF3F2', badge: '#FEE4E2', text: '#D1293D' },
  MEDIUM: { bg: '#FFFBF0', badge: '#FEF3E6', text: '#EF9F27' },
  LOW: { bg: '#F0F4FD', badge: '#E3EDFD', text: '#1570EF' },
}

export function RecommendationsBlock({ recommendations }: Props) {
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
        Рекомендации по улучшению
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {recommendations.map((rec) => {
          const colors = priorityColors[rec.priority]
          return (
            <div
              key={rec.id}
              style={{
                padding: 12,
                background: colors.bg,
                borderRadius: 8,
                borderLeft: `3px solid ${colors.text}`,
                display: 'flex',
                gap: 12,
              }}
            >
              <div
                style={{
                  padding: '4px 8px',
                  background: colors.badge,
                  borderRadius: 4,
                  fontSize: 10,
                  fontWeight: 600,
                  color: colors.text,
                  whiteSpace: 'nowrap',
                  flexShrink: 0,
                }}
              >
                {rec.priority === 'HIGH' ? 'ВЫСОКИЙ' : rec.priority === 'MEDIUM' ? 'СРЕДНИЙ' : 'НИЗКИЙ'}
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 4 }}>
                <div
                  style={{
                    fontSize: 12,
                    fontWeight: 500,
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {rec.title}
                </div>
                <div
                  style={{
                    fontSize: 11,
                    color: 'var(--color-text-secondary, #5f5a52)',
                    lineHeight: '1.4',
                  }}
                >
                  {rec.description}
                </div>
                <div
                  style={{
                    fontSize: 10,
                    color: colors.text,
                    fontWeight: 500,
                    marginTop: 2,
                  }}
                >
                  +{rec.potentialImpact}% потенциального влияния
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
