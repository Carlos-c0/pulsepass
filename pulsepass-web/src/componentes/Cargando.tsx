import clsx from 'clsx'

interface CargandoProps {
    filas?: number
    // "tarjetas": rejilla de esqueletos como la cartelera; "lista": bloques apilados
    forma?: 'tarjetas' | 'lista'
    className?: string
}

// Esqueleto con la forma aproximada del contenido (9.20)
export default function Cargando({ filas = 3, forma = 'lista', className }: CargandoProps) {
    return (
        <div
            aria-busy="true"
            aria-live="polite"
            className={clsx(
                forma === 'tarjetas' ? 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3' : 'space-y-4',
                className,
            )}
        >
            <span className="sr-only">Cargando…</span>
            {Array.from({ length: filas }, (_, i) =>
                forma === 'tarjetas' ? (
                    <div
                        key={i}
                        className="animate-pulse rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
                    >
                        <div className="h-28 rounded-xl bg-slate-100 dark:bg-slate-800" />
                        <div className="mt-4 h-3 w-2/5 rounded-full bg-slate-100 dark:bg-slate-800" />
                        <div className="mt-3 h-4 w-4/5 rounded-full bg-slate-100 dark:bg-slate-800" />
                        <div className="mt-3 h-3 w-3/5 rounded-full bg-slate-100 dark:bg-slate-800" />
                    </div>
                ) : (
                    <div
                        key={i}
                        className="h-20 animate-pulse rounded-2xl border border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900"
                    />
                ),
            )}
        </div>
    )
}
