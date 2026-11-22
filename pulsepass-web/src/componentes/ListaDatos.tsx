import type { ReactNode } from 'react'

// Filas "etiqueta ........ valor" con separador, como en el detalle de sede o de ticket
export function ListaDatos({ children }: { children: ReactNode }) {
    return <dl className="divide-y divide-slate-200 dark:divide-slate-800">{children}</dl>
}

export function Dato({ etiqueta, children }: { etiqueta: string; children: ReactNode }) {
    return (
        <div className="flex items-center justify-between gap-4 py-3">
            <dt className="text-sm text-slate-600 dark:text-slate-400">{etiqueta}</dt>
            <dd className="text-right font-medium">{children}</dd>
        </div>
    )
}
