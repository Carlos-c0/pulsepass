import clsx from 'clsx'
import type { ReactNode } from 'react'

interface TarjetaProps {
    children: ReactNode
    className?: string
}

export default function Tarjeta({ children, className }: TarjetaProps) {
    return (
        <div
            className={clsx(
                'rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900',
                className,
            )}
        >
            {children}
        </div>
    )
}
