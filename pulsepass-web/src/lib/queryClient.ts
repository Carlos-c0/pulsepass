import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

// 4xx no se reintenta (un 404 o un 409 no cambian solos); red y 5xx, una vez.
// Las mutaciones nunca se reintentan: una compra repetida seria un ticket de mas.
export function crearQueryClient(): QueryClient {
    return new QueryClient({
        defaultOptions: {
            queries: {
                retry: (intentos, error) => {
                    if (error instanceof ApiError && error.status >= 400 && error.status < 500) return false
                    return intentos < 1
                },
                refetchOnWindowFocus: false,
            },
            mutations: { retry: false },
        },
    })
}
