interface Props {
  message?: string
  onRetry?: () => void
}

export function InlineError({ message = 'Не удалось загрузить данные', onRetry }: Props) {
  return (
    <div
      style={{
        background: 'var(--color-background-secondary, #ece9e2)',
        borderRadius: 12,
        padding: '24px 20px',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 12,
      }}
    >
      <div style={{ fontSize: 13, color: 'var(--color-text-secondary, #5f5a52)' }}>
        {message}
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          style={{
            padding: '8px 16px',
            background: 'var(--color-background-primary, #ffffff)',
            border: '0.5px solid var(--color-border-tertiary, #dad7cf)',
            borderRadius: 6,
            fontSize: 12,
            fontWeight: 500,
            cursor: 'pointer',
            color: 'var(--color-text-primary, #1d1d1b)',
          }}
        >
          Повторить
        </button>
      )}
    </div>
  )
}
