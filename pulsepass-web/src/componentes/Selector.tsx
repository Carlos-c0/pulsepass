import clsx from 'clsx'
import { useId, type ComponentProps } from 'react'
import { EnvolturaCampo } from './Campo'
import { bordeControl, describirCampo, estilosControl } from './estilos'

interface SelectorProps extends ComponentProps<'select'> {
    etiqueta: string
    error?: string
}

export default function Selector({
                                     etiqueta,
                                     error,
                                     id,
                                     className,
                                     children,
                                     ...props
                                 }: SelectorProps) {
    const idGenerado = useId()
    const idCampo = id ?? idGenerado

    return (
        <EnvolturaCampo idCampo={idCampo} etiqueta={etiqueta} error={error} className={className}>
            <select
                id={idCampo}
                aria-invalid={error ? true : undefined}
                aria-describedby={describirCampo(idCampo, error)}
                className={clsx(estilosControl, bordeControl(error))}
                {...props}
            >
                {children}
            </select>
        </EnvolturaCampo>
    )
}
