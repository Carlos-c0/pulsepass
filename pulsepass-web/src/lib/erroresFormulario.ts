import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError } from './http'

// Un 400 trae details { campo: mensaje }. Cada clave que exista en el formulario va a su campo;
// las demas se devuelven para mostrarlas en un aviso general (PRD 11.2).
export function aplicarErroresDeCampos<T extends FieldValues>(
    error: unknown,
    campos: readonly Path<T>[],
    setError: UseFormSetError<T>,
): string[] {
    if (!(error instanceof ApiError) || error.status !== 400) return []
    const sueltos: string[] = []
    for (const [campo, mensaje] of Object.entries(error.details)) {
        const nombre = campos.find((c) => c === campo)
        if (nombre) setError(nombre, { message: mensaje })
        else sueltos.push(mensaje)
    }
    return sueltos
}
