import type { PipelineHealth } from '../../types'

interface Props {
  pipeline: PipelineHealth
}

const statusColors: Record<string, { bg: string; icon: string; label: string; border?: string }> = {
  UP: { bg: '#E8F5E9', icon: '●', label: 'Работает', border: undefined },
  DOWN: { bg: '#FFEBEE', icon: '●', label: 'Ошибка', border: '3px solid #E24B4A' },
  DEGRADED: { bg: '#FFF3E0', icon: '●', label: 'Проблемы', border: '3px solid #EF9F27' },
}

const statusTextColors: Record<string, string> = {
  UP: '#1D9E75',
  DOWN: '#E24B4A',
  DEGRADED: '#EF9F27',
}

export function PipelineHealthCard({ pipeline }: Props) {
  const colors = statusColors[pipeline.status]

  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: colors.border || '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        padding: 16,
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
      }}
    >
      <div>
        <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 8 }}>
          {pipeline.name.charAt(0).toUpperCase() + pipeline.name.slice(1)}
        </div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            fontSize: 24,
          }}
        >
          <span style={{ color: statusTextColors[pipeline.status] }}>●</span>
          <span
            style={{
              fontSize: 14,
              fontWeight: 500,
              color: statusTextColors[pipeline.status],
            }}
          >
            {colors.label}
          </span>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, fontSize: 12 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ color: 'var(--color-text-secondary, #5f5a52)' }}>Последний успешный запуск:</span>
          <span style={{ fontWeight: 500 }}>
            {new Date(pipeline.lastSuccessfulRun).toLocaleString('ru-RU')}
          </span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ color: 'var(--color-text-secondary, #5f5a52)' }}>Обработано записей:</span>
          <span style={{ fontWeight: 500 }}>{pipeline.recordsProcessed.toLocaleString('ru')}</span>
        </div>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            color: pipeline.errorCount > 0 ? '#E24B4A' : undefined,
          }}
        >
          <span style={{ color: 'var(--color-text-secondary, #5f5a52)' }}>Ошибки:</span>
          <span style={{ fontWeight: 500 }}>{pipeline.errorCount}</span>
        </div>
      </div>
    </div>
  )
}
