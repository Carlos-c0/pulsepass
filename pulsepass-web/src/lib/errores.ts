import { ApiError } from './http'

// Traduce cualquier error a un texto para la persona (PRD seccion 13).
// El mensaje del backend solo se muestra en 409 y 400: son reglas de negocio o validaciones.
export function mensajeDeError(error: unknown): string {
    if (!(error instanceof ApiError)) return 'Ocurrió un error inesperado. Inténtalo más tarde.'

    switch (error.status) {
        case 0:
            return 'No pudimos conectar con el servidor.'
        case 400:
        case 409:
            return error.message
        case 401:
            return 'Tu sesión expiró. Inicia sesión de nuevo.'
        case 403:
            return 'No tienes permisos para hacer esto.'
        case 404:
            return 'No encontramos lo que buscabas.'
        default:
            return 'Ocurrió un error inesperado. Inténtalo más tarde.'
    }
}

export function esEstado(error: unknown, ...estados: number[]): boolean {
    return error instanceof ApiError && estados.includes(error.status)
}
