interface Props {
  label: string
  value: string | number
  delta?: number
  deltaLabel?: string
}

export function KpiCard({ label, value, delta, deltaLabel }: Props) {
  const isUp = delta !== undefined && delta >= 0
  const color =
    delta === undefined
      ? 'var(--color-text-secondary)'
      : isUp
        ? '#3B6D11'
        : '#A32D2D'

  return (
    <div
      style={{
        background: 'var(--color-background-secondary, #ece9e2)',
        borderRadius: 8,
        padding: '14px 16px',
      }}
    >
      <div
        style={{
          fontSize: 12,
          color: 'var(--color-text-secondary, #5f5a52)',
          marginBottom: 6,
        }}
      >
        {label}
      </div>
      <div style={{ fontSize: 24, fontWeight: 500 }}>{value}</div>
      {delta !== undefined && (
        <div style={{ fontSize: 12, color, marginTop: 4 }}>
          {isUp ? '+' : ''}
          {delta} {deltaLabel}
        </div>
      )}
    </div>
  )
}
