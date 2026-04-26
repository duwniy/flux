import { useState } from 'react'

interface Props {
  isOpen: boolean
  onClose: () => void
  onCreate: (name: string, weights: Record<string, number>) => Promise<void>
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

const factors = ['description', 'photos', 'title', 'floor_info', 'seller_type', 'price_competitiveness', 'demand_context', 'competitor_density']

export function CreateModelModal({ isOpen, onClose, onCreate }: Props) {
  const [name, setName] = useState('')
  const [weights, setWeights] = useState<Record<string, number>>({
    description: 15,
    photos: 20,
    title: 12,
    floor_info: 8,
    seller_type: 10,
    price_competitiveness: 18,
    demand_context: 10,
    competitor_density: 7,
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const totalWeight = Object.values(weights).reduce((a, b) => a + b, 0)
  const isValid = name.trim().length > 0 && totalWeight > 0

  const handleSubmit = async () => {
    if (!isValid) return
    setIsSubmitting(true)
    try {
      await onCreate(name, weights)
      setName('')
      setWeights({
        description: 15,
        photos: 20,
        title: 12,
        floor_info: 8,
        seller_type: 10,
        price_competitiveness: 18,
        demand_context: 10,
        competitor_density: 7,
      })
      onClose()
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!isOpen) return null

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        background: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: 'var(--color-background-primary, #ffffff)',
          borderRadius: 12,
          padding: 24,
          maxWidth: 500,
          maxHeight: '80vh',
          overflow: 'auto',
          boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 style={{ fontSize: 16, fontWeight: 500, margin: '0 0 16px 0' }}>Создать новую модель</h2>

        <div style={{ marginBottom: 16 }}>
          <label style={{ fontSize: 12, fontWeight: 500, display: 'block', marginBottom: 6 }}>
            Название модели
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Введите название модели"
            style={{
              width: '100%',
              padding: '8px 10px',
              border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
              borderRadius: 6,
              fontSize: 13,
              boxSizing: 'border-box',
            }}
          />
        </div>

        <div style={{ marginBottom: 16 }}>
          <h3 style={{ fontSize: 12, fontWeight: 500, margin: '0 0 12px 0' }}>Веса факторов</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {factors.map((factor) => (
              <div key={factor} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <label style={{ fontSize: 12, width: 120, flexShrink: 0 }}>
                  {factorLabels[factor]}
                </label>
                <input
                  type="number"
                  min="0"
                  max="100"
                  value={weights[factor]}
                  onChange={(e) => setWeights({ ...weights, [factor]: parseInt(e.target.value) || 0 })}
                  style={{
                    width: 60,
                    padding: '6px 8px',
                    border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                    borderRadius: 4,
                    fontSize: 12,
                  }}
                />
              </div>
            ))}
          </div>
        </div>

        <div
          style={{
            padding: 10,
            background: totalWeight === 100 ? '#E8F5E9' : '#FFF3E0',
            borderRadius: 6,
            fontSize: 12,
            marginBottom: 16,
            color: totalWeight === 100 ? '#1D9E75' : '#EF9F27',
          }}
        >
          Сумма весов: {totalWeight}
          {totalWeight !== 100 && <div style={{ fontSize: 11, marginTop: 2 }}>Рекомендуется 100</div>}
        </div>

        <div style={{ display: 'flex', gap: 8 }}>
          <button
            onClick={onClose}
            style={{
              flex: 1,
              padding: '10px 12px',
              background: 'var(--color-background-secondary, #ece9e2)',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            Отменить
          </button>
          <button
            onClick={handleSubmit}
            disabled={!isValid || isSubmitting}
            style={{
              flex: 1,
              padding: '10px 12px',
              background: isValid ? '#1D9E75' : '#ccc',
              color: 'white',
              border: 'none',
              borderRadius: 6,
              fontSize: 13,
              fontWeight: 500,
              cursor: isValid && !isSubmitting ? 'pointer' : 'not-allowed',
              opacity: isSubmitting ? 0.7 : 1,
            }}
          >
            {isSubmitting ? 'Сохраняю...' : 'Сохранить'}
          </button>
        </div>
      </div>
    </div>
  )
}
