import { QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { AvisosProvider } from '../componentes/Avisos'
import { AuthProvider } from '../funcionalidades/auth/AuthContext'
import type { Rol } from '../funcionalidades/auth/tipos'
import { crearQueryClient } from '../lib/queryClient'
import { guardarToken } from '../lib/sesionToken'
import { rutas } from '../router'

interface Opciones {
    ruta?: string
    // Con un rol, la prueba arranca con sesion iniciada (el token falso lo entiende el mock de /me)
    rol?: Rol
}

export function renderConProveedores({ ruta = '/', rol }: Opciones = {}) {
    if (rol) guardarToken(`token-${rol}`, 900)

    const queryClient = crearQueryClient()
    queryClient.setDefaultOptions({ queries: { retry: false, refetchOnWindowFocus: false } })
    const router = createMemoryRouter(rutas, { initialEntries: [ruta] })
    const user = userEvent.setup()

    const resultado = render(
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <AvisosProvider>
                    <RouterProvider router={router} />
                </AvisosProvider>
            </AuthProvider>
        </QueryClientProvider>,
    )

    return { ...resultado, router, user, queryClient }
}
