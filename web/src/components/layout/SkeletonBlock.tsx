interface Props {
  rows?: number
  height?: number
  gap?: number
}

export function SkeletonBlock({ rows = 3, height = 18, gap = 10 }: Props) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap }}>
      {Array.from({ length: rows }, (_, i) => (
        <div
          key={i}
          style={{
            height,
            borderRadius: 6,
            background: 'var(--color-background-secondary, #ece9e2)',
            animation: 'skeletonPulse 1.5s ease-in-out infinite',
            width: i === rows - 1 ? '60%' : '100%',
          }}
        />
      ))}
    </div>
  )
}
