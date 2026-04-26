import type { DistrictScore } from '../../types'

interface Props {
  data: DistrictScore[]
}

const getScoreBadgeColor = (score: number) => {
  if (score >= 70) return { bg: '#E6F9F0', text: '#1D9E75' }
  if (score >= 40) return { bg: '#FEF3E0', text: '#EF9F27' }
  return { bg: '#FEE3E3', text: '#E24B4A' }
}

export function ConversionFunnelTable({ data }: Props) {
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
              Средний скоринг
            </th>
            <th
              style={{
                textAlign: 'right',
                padding: '10px 8px',
                fontWeight: 500,
                color: 'var(--color-text-secondary, #5f5a52)',
              }}
            >
              Количество звонков
            </th>
          </tr>
        </thead>
        <tbody>
          {data.map((row) => {
            const colors = getScoreBadgeColor(row.avgScore)
            return (
              <tr
                key={row.districtId}
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
                  {row.districtName}
                </td>
                <td
                  style={{
                    textAlign: 'right',
                    padding: '10px 8px',
                  }}
                >
                  <div
                    style={{
                      display: 'inline-block',
                      background: colors.bg,
                      color: colors.text,
                      padding: '4px 8px',
                      borderRadius: 4,
                      fontWeight: 500,
                    }}
                  >
                    {row.avgScore.toFixed(1)}
                  </div>
                </td>
                <td
                  style={{
                    textAlign: 'right',
                    padding: '10px 8px',
                    color: 'var(--color-text-primary, #1d1d1b)',
                  }}
                >
                  {Math.round(row.listingCount * 0.26).toLocaleString('ru')}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
