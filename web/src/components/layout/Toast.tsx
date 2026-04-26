import { useEffect, useState } from 'react'

interface ToastItem {
  id: number
  text: string
}

let toastId = 0
let addToastFn: ((text: string) => void) | null = null

export function showToast(text: string) {
  addToastFn?.(text)
}

export function ToastContainer() {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  useEffect(() => {
    addToastFn = (text: string) => {
      const id = ++toastId
      setToasts((prev) => [...prev, { id, text }])
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id))
      }, 4000)
    }
    return () => {
      addToastFn = null
    }
  }, [])

  if (toasts.length === 0) return null

  return (
    <div
      style={{
        position: 'fixed',
        bottom: 20,
        right: 20,
        display: 'flex',
        flexDirection: 'column',
        gap: 8,
        zIndex: 1000,
      }}
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          style={{
            background: '#E24B4A',
            color: '#ffffff',
            padding: '12px 16px',
            borderRadius: 8,
            fontSize: 13,
            fontWeight: 500,
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
            maxWidth: 360,
            animation: 'toastSlideIn 200ms ease-out',
          }}
        >
          {toast.text}
        </div>
      ))}
    </div>
  )
}
