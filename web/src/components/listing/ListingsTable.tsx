import { useNavigate } from 'react-router-dom'
import type { Listing } from '../../types'

interface Props {
  listings: Listing[]
}

const getScoreBadgeColor = (score: number) => {
  if (score >= 70) return { bg: '#E6F9F0', text: '#1D9E75' }
  if (score >= 40) return { bg: '#FEF3E0', text: '#EF9F27' }
  return { bg: '#FEE3E3', text: '#E24B4A' }
}

const getEnrichmentColor = (status: string) => {
  if (status === 'ENRICHED') return { bg: '#E6F9F0', text: '#1D9E75' }
  if (status === 'PENDING') return { bg: '#FEF3E0', text: '#EF9F27' }
  return { bg: '#FEE3E3', text: '#E24B4A' }
}

const enrichmentStatusLabel: Record<string, string> = {
  ENRICHED: 'Обогащено',
  PENDING: 'В ожидании',
  FAILED: 'Ошибка',
}

const sellerTypeLabel: Record<string, string> = {
  OWNER: 'Собственник',
  AGENCY: 'Агентство',
  DEVELOPER: 'Застройщик',
}

export function ListingsTable({ listings }: Props) {
  const navigate = useNavigate()

  const formatDate = (dateString: string) => {
    if (!dateString) return '—'
    const date = new Date(dateString)
    return date.toLocaleDateString('ru-RU', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    })
  }

  return (
    <div style={{ overflowX: 'auto' }}>
      <table
        style={{
          width: '100%',
          borderCollapse: 'collapse',
          fontSize: 13,
        }}
      >
        <thead>
          <tr
            style={{
              borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            }}
          >
            <th
              style={{
                textAlign: 'left',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Название
            </th>
            <th
              style={{
                textAlign: 'left',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Район
            </th>
            <th
              style={{
                textAlign: 'right',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Цена
            </th>
            <th
              style={{
                textAlign: 'right',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Площадь
            </th>
            <th
              style={{
                textAlign: 'center',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Скоринг
            </th>
            <th
              style={{
                textAlign: 'left',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Тип продавца
            </th>
            <th
              style={{
                textAlign: 'left',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Статус обогащения
            </th>
            <th
              style={{
                textAlign: 'left',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Дата создания
            </th>
            <th
              style={{
                textAlign: 'center',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Действия
            </th>
          </tr>
        </thead>
        <tbody>
          {listings.map((listing) => {
            const scoreColors = getScoreBadgeColor(listing.score)
            const enrichmentColors = getEnrichmentColor(listing.enrichmentStatus)

            return (
              <tr
                key={listing.id}
                style={{
                  borderBottom: '0.5px solid var(--color-border-tertiary, #dad7cf)',
                }}
              >
                <td
                  style={{
                    padding: '10px 8px',
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  <button
                    onClick={() => navigate(`/listings/${listing.id}`)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: 'var(--color-primary, #0066ff)',
                      cursor: 'pointer',
                      padding: 0,
                      textDecoration: 'none',
                      fontSize: 'inherit',
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.textDecoration = 'underline'
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.textDecoration = 'none'
                    }}
                  >
                    {listing.title}
                  </button>
                </td>
                <td
                  style={{
                    padding: '10px 8px',
                    color: 'var(--color-text-secondary, #5f5a52)',
                  }}
                >
                  {listing.districtId}
                </td>
                <td
                  style={{
                    textAlign: 'right',
                    padding: '10px 8px',
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {listing.price.toLocaleString('ru-RU')}₽
                </td>
                <td
                  style={{
                    textAlign: 'right',
                    padding: '10px 8px',
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {listing.totalAreaSqm}м²
                </td>
                <td
                  style={{
                    textAlign: 'center',
                    padding: '10px 8px',
                  }}
                >
                  <div
                    style={{
                      display: 'inline-block',
                      background: scoreColors.bg,
                      color: scoreColors.text,
                      padding: '4px 8px',
                      borderRadius: 4,
                      fontWeight: 500,
                    }}
                  >
                    {listing.score}
                  </div>
                </td>
                <td
                  style={{
                    padding: '10px 8px',
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {sellerTypeLabel[listing.sellerType]}
                </td>
                <td
                  style={{
                    padding: '10px 8px',
                  }}
                >
                  <div
                    style={{
                      display: 'inline-block',
                      background: enrichmentColors.bg,
                      color: enrichmentColors.text,
                      padding: '4px 8px',
                      borderRadius: 4,
                      fontWeight: 500,
                    }}
                  >
                    {enrichmentStatusLabel[listing.enrichmentStatus]}
                  </div>
                </td>
                <td
                  style={{
                    padding: '10px 8px',
                    color: 'var(--color-text-secondary, #5f5a52)',
                  }}
                >
                  {formatDate(listing.createdAt)}
                </td>
                <td
                  style={{
                    textAlign: 'center',
                    padding: '10px 8px',
                  }}
                >
                  <button
                    onClick={() => navigate(`/listings/${listing.id}`)}
                    style={{
                      background: 'var(--color-background-secondary, #ece9e2)',
                      border: 'none',
                      borderRadius: 4,
                      padding: '6px 12px',
                      fontSize: 12,
                      cursor: 'pointer',
                      color: 'var(--color-text-primary, #1d1d1b)',
                      fontWeight: 500,
                      transition: 'background 200ms',
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.background =
                        'var(--color-background-tertiary, #d7d2c8)'
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.background =
                        'var(--color-background-secondary, #ece9e2)'
                    }}
                  >
                    Открыть
                  </button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
