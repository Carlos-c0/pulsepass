import clsx from 'clsx'
import { useId, type ComponentProps, type ReactNode } from 'react'
import { EnvolturaCampo } from './Campo'
import { bordeControl, describirCampo, estilosControl } from './estilos'

interface AreaTextoProps extends ComponentProps<'textarea'> {
    etiqueta: string
    error?: string
    ayuda?: ReactNode
}

export default function AreaTexto({
                                      etiqueta,
                                      error,
                                      ayuda,
                                      id,
                                      className,
                                      ...props
                                  }: AreaTextoProps) {
    const idGenerado = useId()
    const idCampo = id ?? idGenerado

    return (
        <EnvolturaCampo
            idCampo={idCampo}
            etiqueta={etiqueta}
            error={error}
            ayuda={ayuda}
            className={className}
        >
      <textarea
          id={idCampo}
          aria-invalid={error ? true : undefined}
          aria-describedby={describirCampo(idCampo, error, ayuda)}
          className={clsx(estilosControl, bordeControl(error), 'min-h-24')}
          {...props}
      />
        </EnvolturaCampo>
    )
}
