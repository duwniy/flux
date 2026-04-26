import type { ListingVersion } from '../../types'

interface Props {
  versions: ListingVersion[]
}

const getScoreColor = (score: number) => {
  if (score >= 80) return '#1D9E75'
  if (score >= 60) return '#EF9F27'
  return '#E24B4A'
}

export function ListingVersionHistory({ versions }: Props) {
  const sortedVersions = [...versions].reverse()

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
        История версий объявления
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        {sortedVersions.map((v, idx) => (
          <div
            key={v.id}
            style={{
              padding: 12,
              background: v.isCurrent ? '#F0FDF4' : 'var(--color-background-secondary, #ece9e2)',
              borderRadius: idx === 0 ? '8px 8px 0 0' : idx === sortedVersions.length - 1 ? '0 0 8px 8px' : 0,
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              borderBottom: idx !== sortedVersions.length - 1 ? '1px solid var(--color-border-tertiary, #dad7cf)' : 'none',
            }}
          >
            <div
              style={{
                width: 60,
                flexShrink: 0,
                display: 'flex',
                flexDirection: 'column',
                gap: 2,
              }}
            >
              <div
                style={{
                  fontSize: 13,
                  fontWeight: 600,
                  color: 'var(--color-text-primary, #1d1d1b)',
                }}
              >
                v{v.versionNumber}
              </div>
              {v.isCurrent && (
                <div
                  style={{
                    fontSize: 10,
                    color: '#1D9E75',
                    fontWeight: 500,
                  }}
                >
                  Текущая
                </div>
              )}
            </div>

            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                flex: 1,
              }}
            >
              <div
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: '50%',
                  border: `3px solid ${getScoreColor(v.score)}`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                <span style={{ fontSize: 14, fontWeight: 600 }}>{v.score}</span>
              </div>

              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 2 }}>
                <div
                  style={{
                    fontSize: 12,
                    color: 'var(--color-text-secondary, #5f5a52)',
                  }}
                >
                  Причина: {v.changeReason}
                </div>
                <div
                  style={{
                    fontSize: 11,
                    color: 'var(--color-text-tertiary, #8f877d)',
                  }}
                >
                  {new Date(v.validFrom).toLocaleDateString('ru-RU')} —{' '}
                  {v.validTo ? new Date(v.validTo).toLocaleDateString('ru-RU') : 'по настоящее время'}
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 16, flexShrink: 0 }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 2, textAlign: 'right' }}>
                <div
                  style={{
                    fontSize: 11,
                    color: 'var(--color-text-tertiary, #8f877d)',
                  }}
                >
                  Цена
                </div>
                <div
                  style={{
                    fontSize: 12,
                    fontWeight: 500,
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {(v.price / 1000000).toFixed(1)}M
                </div>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 2, textAlign: 'right' }}>
                <div
                  style={{
                    fontSize: 11,
                    color: 'var(--color-text-tertiary, #8f877d)',
                  }}
                >
                  Фото
                </div>
                <div
                  style={{
                    fontSize: 12,
                    fontWeight: 500,
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {v.photosCount}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
