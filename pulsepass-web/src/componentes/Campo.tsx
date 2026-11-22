import clsx from 'clsx'
import { useId, useState, type ComponentProps, type ReactNode } from 'react'
import { bordeControl, describirCampo, estilosControl } from './estilos'
import { IconoAlerta, IconoOjo, IconoOjoTachado } from './Iconos'

interface EnvolturaProps {
    idCampo: string
    etiqueta: string
    error?: string
    ayuda?: ReactNode
    className?: string
    children: ReactNode
}

// Etiqueta, control, texto de ayuda y error: lo comparten Campo, Selector y AreaTexto
export function EnvolturaCampo({
                                   idCampo,
                                   etiqueta,
                                   error,
                                   ayuda,
                                   className,
                                   children,
                               }: EnvolturaProps) {
    return (
        <div className={className}>
            <label
                htmlFor={idCampo}
                className="mb-1.5 block text-sm font-medium text-slate-800 dark:text-slate-200"
            >
                {etiqueta}
            </label>
            {children}
            {ayuda && !error && (
                <p
                    id={`${idCampo}-ayuda`}
                    className="mt-1.5 text-[13px] text-slate-500 dark:text-slate-400"
                >
                    {ayuda}
                </p>
            )}
            {error && (
                <p
                    id={`${idCampo}-error`}
                    className="mt-1.5 flex items-center gap-1.5 text-[13px] text-red-600 dark:text-red-400"
                >
                    <IconoAlerta className="size-3.5 shrink-0" />
                    {error}
                </p>
            )}
        </div>
    )
}

interface CampoProps extends ComponentProps<'input'> {
    etiqueta: string
    error?: string
    ayuda?: ReactNode
    icono?: ReactNode
}

export default function Campo({
                                  etiqueta,
                                  error,
                                  ayuda,
                                  icono,
                                  id,
                                  type = 'text',
                                  className,
                                  ...props
                              }: CampoProps) {
    const idGenerado = useId()
    const idCampo = id ?? idGenerado
    const [visible, setVisible] = useState(false)
    const esClave = type === 'password'

    return (
        <EnvolturaCampo
            idCampo={idCampo}
            etiqueta={etiqueta}
            error={error}
            ayuda={ayuda}
            className={className}
        >
            <div className="relative">
                {icono && (
                    <span className="pointer-events-none absolute inset-y-0 left-3.5 flex items-center text-slate-500">
            {icono}
          </span>
                )}
                <input
                    id={idCampo}
                    type={esClave && visible ? 'text' : type}
                    aria-invalid={error ? true : undefined}
                    aria-describedby={describirCampo(idCampo, error, ayuda)}
                    className={clsx(
                        estilosControl,
                        bordeControl(error),
                        icono && 'pl-10',
                        esClave && 'pr-11',
                    )}
                    {...props}
                />
                {esClave && (
                    <button
                        type="button"
                        onClick={() => setVisible((v) => !v)}
                        aria-pressed={visible}
                        className="focus-visible:outline-marca-600 absolute inset-y-0 right-1 my-auto grid size-9 place-items-center rounded-md text-slate-500 hover:text-slate-800 focus-visible:outline-2 dark:hover:text-slate-200"
                    >
                        {visible ? (
                            <IconoOjoTachado className="size-[18px]" />
                        ) : (
                            <IconoOjo className="size-[18px]" />
                        )}
                        <span className="sr-only">{visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}</span>
                    </button>
                )}
            </div>
        </EnvolturaCampo>
    )
}
