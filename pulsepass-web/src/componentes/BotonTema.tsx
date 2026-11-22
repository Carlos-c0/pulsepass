import { useEffect, useState } from 'react'
import { IconoLuna, IconoSol } from './Iconos'

type Tema = 'claro' | 'oscuro'
const CLAVE = 'pulsepass.tema'

// La preferencia del tema no es un dato sensible, por eso si puede ir en localStorage
function temaInicial(): Tema {
    try {
        const guardado = localStorage.getItem(CLAVE)
        if (guardado === 'claro' || guardado === 'oscuro') return guardado
    } catch {
        // sin almacenamiento: se usa la preferencia del sistema
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro'
}

export default function BotonTema() {
    const [tema, setTema] = useState<Tema>(temaInicial)

    useEffect(() => {
        document.documentElement.classList.toggle('dark', tema === 'oscuro')
        try {
            localStorage.setItem(CLAVE, tema)
        } catch {
            // no pasa nada si no se puede guardar
        }
    }, [tema])

    const siguiente: Tema = tema === 'oscuro' ? 'claro' : 'oscuro'
    return (
        <button
            type="button"
            onClick={() => setTema(siguiente)}
            className="focus-visible:outline-marca-600 rounded-lg p-2 text-slate-600 hover:bg-slate-100 focus-visible:outline-2 dark:text-slate-300 dark:hover:bg-slate-800"
        >
            {tema === 'oscuro' ? <IconoSol /> : <IconoLuna />}
            <span className="sr-only">Cambiar a modo {siguiente}</span>
        </button>
    )
}
