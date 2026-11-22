import { useEffect, useId, useRef, type ReactNode } from 'react'
import { IconoCerrar } from './Iconos'

interface ModalProps {
    abierto: boolean
    titulo: string
    onCerrar: () => void
    children: ReactNode
    acciones?: ReactNode
}

// Usa <dialog> nativo: showModal() ya atrapa el foco y Esc dispara "cancel"
export default function Modal({ abierto, titulo, onCerrar, children, acciones }: ModalProps) {
    const ref = useRef<HTMLDialogElement>(null)
    const idTitulo = useId()

    useEffect(() => {
        const dialogo = ref.current
        if (!dialogo) return
        if (abierto && !dialogo.open) {
            // jsdom no implementa showModal; en ese caso basta con el atributo open
            if (typeof dialogo.showModal === 'function') dialogo.showModal()
            else dialogo.setAttribute('open', '')
        }
        if (!abierto && dialogo.open) {
            if (typeof dialogo.close === 'function') dialogo.close()
            else dialogo.removeAttribute('open')
        }
    }, [abierto])

    if (!abierto) return null

    return (
        <dialog
            ref={ref}
            aria-labelledby={idTitulo}
            onCancel={(e) => {
                e.preventDefault()
                onCerrar()
            }}
            className="mx-auto mt-[12vh] w-[calc(100%-2rem)] max-w-[30rem] rounded-2xl bg-white p-0 text-slate-900 shadow-2xl backdrop:bg-slate-900/55 dark:bg-slate-900 dark:text-slate-100"
        >
            <div className="flex items-center justify-between border-b border-slate-200 px-5 py-5 dark:border-slate-800">
                <h2 id={idTitulo} className="text-lg font-semibold">
                    {titulo}
                </h2>
                <button
                    type="button"
                    onClick={onCerrar}
                    className="focus-visible:outline-marca-600 rounded-md p-1.5 text-slate-600 hover:bg-slate-100 focus-visible:outline-2 dark:text-slate-300 dark:hover:bg-slate-800"
                >
                    <IconoCerrar />
                    <span className="sr-only">Cerrar</span>
                </button>
            </div>
            <div className="space-y-4 px-5 py-4">{children}</div>
            {acciones && <div className="flex justify-end gap-3 px-5 pb-5">{acciones}</div>}
        </dialog>
    )
}
