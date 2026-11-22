import clsx from 'clsx'
import type { ComponentProps } from 'react'
import { estiloBoton, TAMANOS, VARIANTES, type Tamano, type Variante } from './estilos'

interface BotonProps extends ComponentProps<'button'> {
    variante?: Variante
    tamano?: Tamano
    cargando?: boolean
}

export default function Boton({
                                  variante = 'primario',
                                  tamano = 'normal',
                                  cargando = false,
                                  disabled,
                                  type = 'button',
                                  className,
                                  children,
                                  ...props
                              }: BotonProps) {
    return (
        <button
            type={type}
            disabled={disabled || cargando}
            aria-busy={cargando || undefined}
            className={clsx(
                estiloBoton,
                TAMANOS[tamano],
                // "Cargando…" se ve gris y apagado en cualquier variante (sistema visual 9.1)
                cargando
                    ? 'bg-slate-200 text-slate-500 dark:bg-slate-800 dark:text-slate-400'
                    : [VARIANTES[variante], 'disabled:opacity-60'],
                className,
            )}
            {...props}
        >
            {cargando ? 'Cargando…' : children}
        </button>
    )
}
