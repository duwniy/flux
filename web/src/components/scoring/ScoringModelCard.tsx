import { useState } from 'react'
import type { ScoringModel } from '../../types'

interface Props {
  model: ScoringModel
  onActivate: (id: string) => void
  onBackfill?: (id: string) => void
}

const factorLabels: Record<string, string> = {
  description: 'Описание',
  photos: 'Фотографии',
  title: 'Заголовок',
  floor_info: 'Этаж/этажность',
  seller_type: 'Тип продавца',
  price_competitiveness: 'Цена vs рынок',
  demand_context: 'Спрос района',
  competitor_density: 'Конкуренция',
}

export function ScoringModelCard({ model, onActivate, onBackfill }: Props) {
  const [isActivating, setIsActivating] = useState(false)

  const maxWeight = Math.max(...Object.values(model.factorWeights))

  const handleActivate = async () => {
    setIsActivating(true)
    try {
      await onActivate(model.id)
    } finally {
      setIsActivating(false)
    }
  }

  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        padding: 16,
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h3 style={{ fontSize: 14, fontWeight: 500, margin: 0 }}>{model.name}</h3>
          <p style={{ fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)', margin: '4px 0 0 0' }}>
            v{model.versionNumber}
          </p>
        </div>
        {model.isActive && (
          <div
            style={{
              background: '#E8F5E9',
              color: '#1D9E75',
              padding: '4px 10px',
              borderRadius: 4,
              fontSize: 11,
              fontWeight: 500,
            }}
          >
            Активная
          </div>
        )}
      </div>

      <div style={{ fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)' }}>
        <p style={{ margin: '0 0 4px 0' }}>Создана: {new Date(model.createdAt).toLocaleDateString('ru-RU')}</p>
        {model.activatedAt && (
          <p style={{ margin: 0 }}>Активирована: {new Date(model.activatedAt).toLocaleDateString('ru-RU')}</p>
        )}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {Object.entries(model.factorWeights).map(([factor, weight]) => (
          <div key={factor} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div style={{ width: 120, fontSize: 12, flexShrink: 0 }}>
              {factorLabels[factor] || factor}
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
                  width: `${(weight / maxWeight) * 100}%`,
                  height: '100%',
                  background: '#1D9E75',
                  borderRadius: 3,
                }}
              />
            </div>
            <div style={{ width: 30, fontSize: 12, textAlign: 'right', fontWeight: 500 }}>{weight}</div>
          </div>
        ))}
      </div>

      <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
        {!model.isActive && (
          <button
            onClick={handleActivate}
            disabled={isActivating}
            style={{
              flex: 1,
              padding: '8px 12px',
              background: '#1D9E75',
              color: 'white',
              border: 'none',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              cursor: isActivating ? 'not-allowed' : 'pointer',
              opacity: isActivating ? 0.6 : 1,
            }}
          >
            {isActivating ? 'Активирую...' : 'Активировать'}
          </button>
        )}
        {onBackfill && (
          <button
            onClick={() => onBackfill(model.id)}
            style={{
              flex: 1,
              padding: '8px 12px',
              background: 'var(--color-background-secondary, #ece9e2)',
              color: 'var(--color-text-primary, #1d1d1b)',
              border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            Запустить backfill
          </button>
        )}
      </div>
    </div>
  )
}
