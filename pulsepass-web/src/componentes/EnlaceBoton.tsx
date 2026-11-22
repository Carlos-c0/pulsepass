import clsx from 'clsx'
import type { ComponentProps } from 'react'
import { Link } from 'react-router'
import { estiloBoton, TAMANOS, VARIANTES, type Tamano, type Variante } from './estilos'

interface EnlaceBotonProps extends ComponentProps<typeof Link> {
    variante?: Variante
    tamano?: Tamano
}

// Un enlace que se ve como boton: navega, asi que en HTML debe ser <a> y no <button>
export default function EnlaceBoton({
                                        variante = 'primario',
                                        tamano = 'normal',
                                        className,
                                        ...props
                                    }: EnlaceBotonProps) {
    return (
        <Link
            className={clsx(estiloBoton, TAMANOS[tamano], VARIANTES[variante], className)}
            {...props}
        />
    )
}
