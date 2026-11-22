import clsx from 'clsx'
import type { ReactNode } from 'react'
import { IconoAlerta, IconoCheck, IconoRayo } from './Iconos'

export type TonoAviso = 'exito' | 'error' | 'advertencia' | 'info'

const ESTILOS: Record<TonoAviso, string> = {
    exito:
        'border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-900 dark:bg-emerald-950 dark:text-emerald-200',
    error:
        'border-red-200 bg-red-50 text-red-800 dark:border-red-900 dark:bg-red-950 dark:text-red-200',
    advertencia:
        'border-amber-200 bg-amber-50 text-amber-800 dark:border-amber-900 dark:bg-amber-950 dark:text-amber-200',
    info: 'border-indigo-200 bg-marca-50 text-marca-700 dark:border-indigo-900 dark:bg-indigo-950 dark:text-indigo-200',
}

const ICONOS: Record<TonoAviso, typeof IconoAlerta> = {
    exito: IconoCheck,
    error: IconoAlerta,
    advertencia: IconoAlerta,
    info: IconoRayo,
}

interface AvisoProps {
    tono: TonoAviso
    children: ReactNode
    // Resultado grande y de alto contraste (validacion en la puerta, 9.14)
    grande?: boolean
    className?: string
}

// Los errores usan role="alert" para que el lector de pantalla los anuncie de inmediato (DES-10)
export default function Aviso({ tono, children, grande = false, className }: AvisoProps) {
    const esProblema = tono === 'error' || tono === 'advertencia'
    const Icono = ICONOS[tono]
    return (
        <div
            role={esProblema ? 'alert' : 'status'}
            className={clsx(
                'flex gap-3 rounded-lg border',
                grande ? 'px-5 py-5 text-lg' : 'px-4 py-3 text-sm',
                ESTILOS[tono],
                className,
            )}
        >
            <Icono className={clsx('shrink-0', grande ? 'mt-1 size-[18px]' : 'mt-0.5 size-4')} />
            <div>{children}</div>
        </div>
    )
}
