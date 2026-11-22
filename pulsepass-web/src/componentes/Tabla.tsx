import type { ReactNode } from 'react'

export interface Columna<T> {
    encabezado: string
    celda: (fila: T) => ReactNode
}

interface TablaProps<T> {
    columnas: Columna<T>[]
    filas: T[]
    clave: (fila: T) => string
    // En movil la tabla se cambia por tarjetas (9.15 / MovilPagados)
    tarjeta: (fila: T) => ReactNode
}

// Generica: sirve para cualquier lista
export default function Tabla<T>({ columnas, filas, clave, tarjeta }: TablaProps<T>) {
    return (
        <>
            <div className="hidden overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm md:block dark:border-slate-800 dark:bg-slate-900">
                <table className="w-full text-left text-[15px]">
                    <thead>
                    <tr className="border-b border-slate-200 text-[13px] text-slate-600 dark:border-slate-800 dark:text-slate-400">
                        {columnas.map((c) => (
                            <th key={c.encabezado} scope="col" className="px-4 py-3 font-semibold">
                                {c.encabezado}
                            </th>
                        ))}
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                    {filas.map((fila) => (
                        <tr key={clave(fila)}>
                            {columnas.map((c) => (
                                <td key={c.encabezado} className="px-4 py-3.5">
                                    {c.celda(fila)}
                                </td>
                            ))}
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
            <ul className="space-y-4 md:hidden">
                {filas.map((fila) => (
                    <li
                        key={clave(fila)}
                        className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
                    >
                        {tarjeta(fila)}
                    </li>
                ))}
            </ul>
        </>
    )
}
