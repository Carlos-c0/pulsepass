import clsx from 'clsx'
import type { ReactNode } from 'react'

export type Tono = 'gris' | 'grisOscuro' | 'verde' | 'ambar' | 'rojo' | 'celeste' | 'marca'

// Mapa tipado de la seccion 9.1 (sin clases armadas con variables)
const TONOS: Record<Tono, string> = {
    gris: 'bg-slate-200 text-slate-700 dark:bg-slate-700 dark:text-slate-200',
    grisOscuro: 'bg-slate-300 text-slate-800 dark:bg-slate-600 dark:text-slate-100',
    verde: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900 dark:text-emerald-200',
    ambar: 'bg-amber-100 text-amber-800 dark:bg-amber-900 dark:text-amber-200',
    rojo: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    celeste: 'bg-sky-100 text-sky-800 dark:bg-sky-900 dark:text-sky-200',
    marca: 'bg-marca-50 text-marca-700 dark:bg-indigo-950 dark:text-indigo-300',
}

interface BadgeProps {
    tono: Tono
    children: ReactNode
}

export default function Badge({ tono, children }: BadgeProps) {
    return (
        <span
            className={clsx(
                'inline-flex shrink-0 items-center rounded-full px-2.5 py-1 text-xs leading-none font-semibold',
                TONOS[tono],
            )}
        >
      {children}
    </span>
    )
}
