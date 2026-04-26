import { useNavigate } from 'react-router-dom'
import type { DataQualityCheck } from '../../types'

interface Props {
  checks: DataQualityCheck[]
}

const triggerReasonBadgeColor: Record<string, { bg: string; color: string }> = {
  MISSING_PHOTOS: { bg: '#FFEBEE', color: '#E24B4A' },
  INVALID_PRICE: { bg: '#FFF3E0', color: '#EF9F27' },
  MISSING_DESCRIPTION: { bg: '#F3E5F5', color: '#7B1FA2' },
  OTHER: { bg: '#E0F2F1', color: '#00695C' },
}

export function QualityReportTable({ checks }: Props) {
  const navigate = useNavigate()

  const getCheckColor = (checkName: string) => {
    if (checkName.includes('photo')) return triggerReasonBadgeColor['MISSING_PHOTOS']
    if (checkName.includes('price')) return triggerReasonBadgeColor['INVALID_PRICE']
    if (checkName.includes('description')) return triggerReasonBadgeColor['MISSING_DESCRIPTION']
    return triggerReasonBadgeColor['OTHER']
  }

  const safeChecks = Array.isArray(checks) ? checks : []

  return (
    <div
      style={{
        background: 'var(--color-background-primary, #ffffff)',
        border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
        borderRadius: 12,
        overflow: 'hidden',
      }}
    >
      <table
        style={{
          width: '100%',
          fontSize: 13,
          borderCollapse: 'collapse',
        }}
      >
        <thead>
          <tr style={{ borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)' }}>
            <th
              style={{
                padding: '12px 16px',
                textAlign: 'left',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
                background: 'var(--color-background-secondary, #ece9e2)',
              }}
            >
              Проверка
            </th>
            <th
              style={{
                padding: '12px 16px',
                textAlign: 'left',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
                background: 'var(--color-background-secondary, #ece9e2)',
              }}
            >
              Сущность
            </th>
            <th
              style={{
                padding: '12px 16px',
                textAlign: 'left',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
                background: 'var(--color-background-secondary, #ece9e2)',
              }}
            >
              Причина
            </th>
            <th
              style={{
                padding: '12px 16px',
                textAlign: 'left',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
                background: 'var(--color-background-secondary, #ece9e2)',
              }}
            >
              Время
            </th>
          </tr>
        </thead>
        <tbody>
          {safeChecks.map((check, idx) => {
            const colors = getCheckColor(check.checkName)
            const isClickable = check.entityType === 'LISTING'
            const checkedAt = check.checkedAt ? new Date(check.checkedAt).toLocaleString('ru-RU') : '-'

            return (
              <tr
                key={idx}
                onClick={() => {
                  if (isClickable) {
                    navigate(`/listings/${check.entityId}`)
                  }
                }}
                style={{
                  borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                  background: 'var(--color-background-danger)',
                  cursor: isClickable ? 'pointer' : 'default',
                  transition: 'background-color 0.2s',
                }}
                onMouseEnter={(e) => {
                  if (isClickable) {
                    e.currentTarget.style.background = 'rgba(226, 75, 74, 0.08)'
                  }
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = 'var(--color-background-danger)'
                }}
              >
                <td style={{ padding: '12px 16px' }}>
                  <div
                    style={{
                      background: colors.bg,
                      color: colors.color,
                      padding: '4px 10px',
                      borderRadius: 4,
                      fontSize: 12,
                      fontWeight: 500,
                      display: 'inline-block',
                    }}
                  >
                    {check.checkName}
                  </div>
                </td>
                <td style={{ padding: '12px 16px' }}>
                  {check.entityType === 'LISTING' ? (
                    <span style={{ color: '#1976D2', textDecoration: 'underline' }}>
                      {check.entityId}
                    </span>
                  ) : (
                    check.entityId
                  )}
                </td>
                <td style={{ padding: '12px 16px', fontSize: 12, color: 'var(--color-text-secondary, #5f5a52)' }}>
                  {check.failureReason}
                </td>
                <td style={{ padding: '12px 16px', fontSize: 12, whiteSpace: 'nowrap' }}>
                  {checkedAt}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
