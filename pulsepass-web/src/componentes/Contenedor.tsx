import clsx from 'clsx'
import type { ReactNode } from 'react'

interface ContenedorProps {
    children: ReactNode
    // DES-04: gestion y formularios van en una columna estrecha centrada
    estrecho?: boolean
    className?: string
}

export default function Contenedor({ children, estrecho = false, className }: ContenedorProps) {
    return (
        <div
            className={clsx(
                'mx-auto w-full px-4 py-6 sm:py-8',
                estrecho ? 'max-w-[52rem]' : 'max-w-6xl',
                className,
            )}
        >
            {children}
        </div>
    )
}

export function TituloPagina({ children, className }: { children: ReactNode; className?: string }) {
    return (
        <h1 className={clsx('text-[1.75rem] leading-tight font-bold sm:text-[2rem]', className)}>
            {children}
        </h1>
    )
}
