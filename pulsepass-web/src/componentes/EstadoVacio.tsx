import type { ReactNode } from 'react'
import { IconoBandeja } from './Iconos'

interface EstadoVacioProps {
    mensaje: string
    accion?: ReactNode
}

export default function EstadoVacio({ mensaje, accion }: EstadoVacioProps) {
    return (
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-slate-200 bg-white px-6 py-8 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <IconoBandeja className="size-9 text-slate-500" />
            <p className="font-semibold">{mensaje}</p>
            {accion}
        </div>
    )
}
