import { z } from 'zod'

// El token vive en memoria y en sessionStorage (se borra al cerrar la pestana).
// Nunca en localStorage, cookies ni la URL (PRD 12.2).
const CLAVE = 'pulsepass.sesion'

const TokenGuardadoSchema = z.object({
    token: z.string().min(1),
    expiraEn: z.number(),
})

export type TokenGuardado = z.infer<typeof TokenGuardadoSchema>

let actual: TokenGuardado | null = null

export function obtenerToken(): string | null {
    return actual?.token ?? null
}

export function guardarToken(
    token: string,
    expiraEnSegundos: number,
    ahora = Date.now(),
): TokenGuardado {
    actual = { token, expiraEn: ahora + expiraEnSegundos * 1000 }
    try {
        sessionStorage.setItem(CLAVE, JSON.stringify(actual))
    } catch {
        // sin sessionStorage la sesion dura solo mientras la pagina este abierta
    }
    return actual
}

export function borrarToken(): void {
    actual = null
    try {
        sessionStorage.removeItem(CLAVE)
    } catch {
        // nada que borrar
    }
}

// Lee lo guardado al arrancar. Si esta mal formado o ya vencio, se descarta.
export function restaurarToken(ahora = Date.now()): TokenGuardado | null {
    let crudo: string | null
    try {
        crudo = sessionStorage.getItem(CLAVE)
    } catch {
        return null
    }
    if (!crudo) return null

    let datos: unknown
    try {
        datos = JSON.parse(crudo)
    } catch {
        borrarToken()
        return null
    }

    const leido = TokenGuardadoSchema.safeParse(datos)
    if (!leido.success || leido.data.expiraEn <= ahora) {
        borrarToken()
        return null
    }
    actual = leido.data
    return actual
}
