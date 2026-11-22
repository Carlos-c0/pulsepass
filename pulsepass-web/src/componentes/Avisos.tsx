import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react'
import Aviso from './Aviso'
import { IconoCerrar } from './Iconos'

// Notificaciones flotantes (toast) para el resultado de las mutaciones (FR-FE-GEN-003)
type TipoNotificacion = 'exito' | 'error' | 'advertencia' | 'info'

interface Notificacion {
    id: number
    tipo: TipoNotificacion
    mensaje: string
}

interface AvisosContextValue {
    notificar: (tipo: TipoNotificacion, mensaje: string) => void
}

const AvisosContext = createContext<AvisosContextValue | null>(null)

let siguienteId = 1

export function AvisosProvider({ children }: { children: ReactNode }) {
    const [lista, setLista] = useState<Notificacion[]>([])

    const cerrar = useCallback((id: number) => {
        setLista((actual) => actual.filter((n) => n.id !== id))
    }, [])

    const notificar = useCallback(
        (tipo: TipoNotificacion, mensaje: string) => {
            const id = siguienteId++
            setLista((actual) => [...actual, { id, tipo, mensaje }])
            // Los avisos de error se quedan hasta que la persona los cierra
            if (tipo === 'exito' || tipo === 'info') setTimeout(() => cerrar(id), 4000)
        },
        [cerrar],
    )

    const valor = useMemo(() => ({ notificar }), [notificar])

    return (
        <AvisosContext.Provider value={valor}>
            {children}
            <div
                aria-live="polite"
                className="fixed inset-x-4 bottom-20 z-50 flex flex-col gap-2 sm:left-auto sm:w-96 md:bottom-4"
            >
                {lista.map((n) => (
                    <div key={n.id} className="relative rounded-lg shadow-lg">
                        <Aviso tono={n.tipo} className="pr-10">
                            {n.mensaje}
                        </Aviso>
                        <button
                            type="button"
                            onClick={() => cerrar(n.id)}
                            className="focus-visible:outline-marca-600 absolute top-2 right-2 rounded p-1 focus-visible:outline-2"
                        >
                            <IconoCerrar className="size-4" />
                            <span className="sr-only">Cerrar aviso</span>
                        </button>
                    </div>
                ))}
            </div>
        </AvisosContext.Provider>
    )
}

export function useAvisos(): AvisosContextValue {
    const contexto = useContext(AvisosContext)
    if (!contexto) throw new Error('useAvisos debe usarse dentro de AvisosProvider')
    return contexto
}
