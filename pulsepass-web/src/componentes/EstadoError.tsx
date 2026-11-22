import { mensajeDeError } from '../lib/errores'
import Aviso from './Aviso'
import Boton from './Boton'

interface EstadoErrorProps {
    error: unknown
    onReintentar?: () => void
}

export default function EstadoError({ error, onReintentar }: EstadoErrorProps) {
    return (
        <div className="flex flex-col items-center gap-3 rounded-2xl border border-slate-200 bg-white p-7 text-center shadow-sm dark:border-slate-800 dark:bg-slate-900">
            <Aviso tono="error" className="w-full max-w-md justify-center">
                {mensajeDeError(error)}
            </Aviso>
            {onReintentar && <Boton onClick={onReintentar}>Reintentar</Boton>}
        </div>
    )
}
