import { z } from 'zod'
import { obtenerToken } from './sesionToken'

const BASE = import.meta.env.VITE_API_BASE_URL ?? '/api'

// Forma de todos los errores del backend (PRD de controladores)
const ErrorResponseSchema = z.object({
    status: z.number(),
    message: z.string(),
    details: z.record(z.string(), z.string()).nullish(),
})

export class ApiError extends Error {
    readonly status: number
    readonly details: Record<string, string>

    constructor(status: number, message: string, details: Record<string, string> = {}) {
        super(message)
        this.name = 'ApiError'
        this.status = status
        this.details = details
    }
}

// Lo registra AuthContext: se llama cuando una peticion con token recibe 401
let alNoAutorizado: (() => void) | null = null

export function registrarAlNoAutorizado(manejador: (() => void) | null): void {
    alNoAutorizado = manejador
}

interface OpcionesPeticion {
    metodo?: 'GET' | 'POST' | 'PATCH'
    cuerpo?: unknown
}

export async function pedir<T>(
    ruta: string,
    esquema: z.ZodType<T>,
    { metodo = 'GET', cuerpo }: OpcionesPeticion = {},
): Promise<T> {
    const token = obtenerToken()
    const headers = new Headers({ Accept: 'application/json' })
    if (cuerpo !== undefined) headers.set('Content-Type', 'application/json')
    if (token) headers.set('Authorization', `Bearer ${token}`)

    let respuesta: Response
    try {
        // Se resuelve contra el origen actual: en el navegador da lo mismo y en las pruebas evita URLs relativas
        respuesta = await fetch(new URL(BASE + ruta, window.location.origin), {
            method: metodo,
            headers,
            body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
        })
    } catch {
        throw new ApiError(0, 'No pudimos conectar con el servidor.')
    }

    if (!respuesta.ok) {
        // Un 401 en el login es "credenciales invalidas"; solo cierra sesion si se envio un token
        if (respuesta.status === 401 && token) alNoAutorizado?.()
        throw await leerError(respuesta)
    }

    const datos: unknown = await respuesta.json()
    return esquema.parse(datos)
}

async function leerError(respuesta: Response): Promise<ApiError> {
    let datos: unknown = null
    try {
        datos = await respuesta.json()
    } catch {
        // el cuerpo no era JSON
    }
    const error = ErrorResponseSchema.safeParse(datos)
    if (error.success) {
        return new ApiError(error.data.status, error.data.message, error.data.details ?? {})
    }
    return new ApiError(respuesta.status, 'Ocurrió un error inesperado. Inténtalo más tarde.')
}
