import clsx from 'clsx'

// Codigos de negocio (CMF-2026, TKT-8F2A1C) en fuente monoespaciada (DES-05)
export default function CodigoTexto({
                                        children,
                                        className,
                                    }: {
    children: string
    className?: string
}) {
    return <span className={clsx('font-mono text-[0.9em]', className)}>{children}</span>
}
