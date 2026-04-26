import { useEffect, useState, useCallback } from 'react'
import { enrichmentApi } from '../../api/enrichment'
import { InlineError } from '../layout/InlineError'
import { SkeletonBlock } from '../layout/SkeletonBlock'
import type { EnrichmentLogEntry } from '../../types'

interface Props {
  listingId: string
  open: boolean
  onClose: () => void
}

export function EnrichmentHistoryPanel({ listingId, open, onClose }: Props) {
  const [logs, setLogs] = useState<EnrichmentLogEntry[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(false)

  const loadHistory = useCallback(async () => {
    setLoading(true)
    setError(false)
    try {
      const data = await enrichmentApi.getHistory(listingId)
      setLogs(data)
    } catch {
      setError(true)
    } finally {
      setLoading(false)
    }
  }, [listingId])

  useEffect(() => {
    if (open) loadHistory()
  }, [open, loadHistory])

  if (!open) return null

  const avgDuration =
    logs.length > 0
      ? Math.round(logs.reduce((sum, l) => sum + l.durationMs, 0) / logs.length)
      : 0

  const lastStatus = logs.length > 0 ? logs[0].status : null

  const formatDuration = (ms: number) =>
    ms.toLocaleString('ru-RU').replace(/,/g, ' ') + ' мс'

  const formatDate = (iso: string) => {
    const d = new Date(iso)
    return d.toLocaleString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    })
  }

  const truncate = (text: string | null, max: number) => {
    if (!text) return ''
    return text.length > max ? text.slice(0, max) + '...' : text
  }

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0, 0, 0, 0.2)',
          zIndex: 900,
        }}
      />

      {/* Panel */}
      <div
        style={{
          position: 'fixed',
          top: 0,
          right: 0,
          width: 560,
          maxWidth: '90vw',
          height: '100vh',
          background: 'var(--color-background-primary, #ffffff)',
          borderLeft: '0.5px solid var(--color-border-tertiary, #dad7cf)',
          boxShadow: '-4px 0 24px rgba(0, 0, 0, 0.08)',
          zIndex: 901,
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {/* Header */}
        <div
          style={{
            padding: '16px 20px',
            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexShrink: 0,
          }}
        >
          <h3 style={{ fontSize: 14, fontWeight: 500, margin: 0 }}>
            История обогащения
          </h3>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: 18,
              cursor: 'pointer',
              color: 'var(--color-text-secondary, #5f5a52)',
              padding: '4px 8px',
              borderRadius: 4,
            }}
          >
            ✕
          </button>
        </div>

        {/* Content */}
        <div style={{ flex: 1, overflow: 'auto', padding: 20 }}>
          {loading && <SkeletonBlock rows={5} height={24} gap={12} />}
          {!loading && error && <InlineError onRetry={loadHistory} />}
          {!loading && !error && logs.length === 0 && (
            <div style={{ fontSize: 13, color: 'var(--color-text-tertiary, #8f877d)', textAlign: 'center', padding: 40 }}>
              Записей обогащения не найдено
            </div>
          )}
          {!loading && !error && logs.length > 0 && (
            <>
              {/* Table */}
              <div style={{ overflowX: 'auto' }}>
                <table
                  style={{
                    width: '100%',
                    borderCollapse: 'collapse',
                    fontSize: 12,
                  }}
                >
                  <thead>
                    <tr>
                      {['Дата / время', 'Статус', 'Время выполнения', 'Ошибка'].map((h) => (
                        <th
                          key={h}
                          style={{
                            textAlign: 'left',
                            padding: '8px 10px',
                            fontWeight: 500,
                            color: 'var(--color-text-secondary, #5f5a52)',
                            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                            whiteSpace: 'nowrap',
                          }}
                        >
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {logs.map((log) => (
                      <tr key={log.id}>
                        <td
                          style={{
                            padding: '10px',
                            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                            whiteSpace: 'nowrap',
                          }}
                        >
                          {formatDate(log.createdAt)}
                        </td>
                        <td
                          style={{
                            padding: '10px',
                            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                          }}
                        >
                          <span
                            style={{
                              display: 'inline-block',
                              padding: '3px 8px',
                              borderRadius: 4,
                              fontSize: 11,
                              fontWeight: 600,
                              background:
                                log.status === 'SUCCESS' ? '#E8F5E9' : '#FFEBEE',
                              color:
                                log.status === 'SUCCESS' ? '#1D9E75' : '#E24B4A',
                            }}
                          >
                            {log.status}
                          </span>
                        </td>
                        <td
                          style={{
                            padding: '10px',
                            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                            whiteSpace: 'nowrap',
                            fontVariantNumeric: 'tabular-nums',
                          }}
                        >
                          {formatDuration(log.durationMs)}
                        </td>
                        <td
                          style={{
                            padding: '10px',
                            borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                            maxWidth: 200,
                          }}
                        >
                          {log.status === 'FAILED' && log.errorMsg && (
                            <span
                              style={{
                                color: '#E24B4A',
                                fontStyle: 'italic',
                                fontSize: 11,
                              }}
                              title={log.errorMsg}
                            >
                              {truncate(log.errorMsg, 80)}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Summary */}
              <div
                style={{
                  marginTop: 16,
                  padding: 14,
                  background: 'var(--color-background-secondary, #ece9e2)',
                  borderRadius: 8,
                  display: 'flex',
                  gap: 24,
                  fontSize: 12,
                }}
              >
                <div>
                  <span style={{ color: 'var(--color-text-secondary, #5f5a52)' }}>
                    Среднее время обогащения:{' '}
                  </span>
                  <strong>{formatDuration(avgDuration)}</strong>
                </div>
                <div>
                  <span style={{ color: 'var(--color-text-secondary, #5f5a52)' }}>
                    Последний статус:{' '}
                  </span>
                  <strong
                    style={{
                      color: lastStatus === 'SUCCESS' ? '#1D9E75' : '#E24B4A',
                    }}
                  >
                    {lastStatus}
                  </strong>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </>
  )
}
