import clsx from 'clsx'

interface Opcion<T extends string> {
    valor: T
    etiqueta: string
}

interface FiltroChipsProps<T extends string> {
    etiqueta: string
    opciones: Opcion<T>[]
    seleccionado: T
    onCambiar: (valor: T) => void
}

// Chips con aria-pressed para que el lector de pantalla diga cual esta activo (DES-06)
export default function FiltroChips<T extends string>({
                                                          etiqueta,
                                                          opciones,
                                                          seleccionado,
                                                          onCambiar,
                                                      }: FiltroChipsProps<T>) {
    return (
        <div role="group" aria-label={etiqueta} className="flex flex-wrap gap-2">
            {opciones.map((o) => {
                const activo = o.valor === seleccionado
                return (
                    <button
                        key={o.valor}
                        type="button"
                        aria-pressed={activo}
                        onClick={() => onCambiar(o.valor)}
                        className={clsx(
                            'focus-visible:outline-marca-600 min-h-11 rounded-full px-5 text-[15px] font-semibold focus-visible:outline-2 focus-visible:outline-offset-2',
                            activo
                                ? 'bg-marca-600 text-white'
                                : 'border border-slate-300 bg-white text-slate-900 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100',
                        )}
                    >
                        {o.etiqueta}
                    </button>
                )
            })}
        </div>
    )
}
