import { useEffect, useState } from 'react'
import { scoringModelsApi } from '../api/scoring'
import { ScoringModelCard } from '../components/scoring/ScoringModelCard'
import { CreateModelModal } from '../components/scoring/CreateModelModal'
import type { ScoringModel } from '../types'

export function ScoringModels() {
  const [models, setModels] = useState<ScoringModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  useEffect(() => {
    loadModels()
  }, [])

  const loadModels = async () => {
    setIsLoading(true)
    try {
      const data = await scoringModelsApi.getModels()
      setModels(data)
    } finally {
      setIsLoading(false)
    }
  }

  const handleActivate = async (id: string) => {
    try {
      await scoringModelsApi.activateModel(id)
      await loadModels()
    } catch (error) {
      setToast('Ошибка при активации модели')
      setTimeout(() => setToast(null), 3000)
    }
  }

  const handleBackfill = async (id: string) => {
    try {
      await scoringModelsApi.runBackfill(id)
      setToast('Пересчёт запущен')
      setTimeout(() => setToast(null), 3000)
    } catch (error) {
      setToast('Ошибка при запуске пересчёта')
      setTimeout(() => setToast(null), 3000)
    }
  }

  const handleCreate = async (name: string, weights: Record<string, number>) => {
    try {
      await scoringModelsApi.createModel(name, weights)
      await loadModels()
      setToast('Модель создана успешно')
      setTimeout(() => setToast(null), 3000)
    } catch (error) {
      setToast('Ошибка при создании модели')
      setTimeout(() => setToast(null), 3000)
    }
  }

  return (
    <div style={{ padding: 20, display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1 style={{ fontSize: 20, fontWeight: 500, margin: 0 }}>Скоринговые модели</h1>
        <button
          onClick={() => setIsModalOpen(true)}
          style={{
            padding: '10px 16px',
            background: '#1D9E75',
            color: 'white',
            border: 'none',
            borderRadius: 6,
            fontSize: 13,
            fontWeight: 500,
            cursor: 'pointer',
          }}
        >
          Создать модель
        </button>
      </div>

      {isLoading ? (
        <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--color-text-secondary, #5f5a52)' }}>
          Загрузка моделей...
        </div>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
            gap: 16,
          }}
        >
          {models.map((model) => (
            <ScoringModelCard
              key={model.id}
              model={model}
              onActivate={handleActivate}
              onBackfill={handleBackfill}
            />
          ))}
        </div>
      )}

      <CreateModelModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onCreate={handleCreate} />

      {toast && (
        <div
          style={{
            position: 'fixed',
            bottom: 20,
            right: 20,
            background: '#1D9E75',
            color: 'white',
            padding: '12px 16px',
            borderRadius: 6,
            fontSize: 13,
            boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
          }}
        >
          {toast}
        </div>
      )}
    </div>
  )
}
