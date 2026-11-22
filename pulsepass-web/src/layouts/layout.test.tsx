import { render, screen, within } from '@testing-library/react'
import { QueryClientProvider } from '@tanstack/react-query'
import { createMemoryRouter, RouterProvider } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../funcionalidades/auth/AuthContext'
import { crearQueryClient } from '../lib/queryClient'
import ErrorPagina from '../pages/ErrorPagina'
import { renderConProveedores } from '../test/utils'

function enlacesDe(nav: HTMLElement) {
    return within(nav)
        .getAllByRole('link')
        .map((a) => a.textContent)
}

describe('Transversales', () => {
    it('TEST-FE-GEN-001: una ruta que no existe muestra el 404', async () => {
        renderConProveedores({ ruta: '/esto/no/existe' })

        expect(
            await screen.findByRole('heading', { name: 'No encontramos esta página.' }),
        ).toBeInTheDocument()
        expect(screen.getByRole('link', { name: 'Volver al inicio' })).toHaveAttribute('href', '/')
    })

    it('TEST-FE-GEN-002: un error inesperado no muestra detalles técnicos', async () => {
        vi.spyOn(console, 'error').mockImplementation(() => {})
        const router = createMemoryRouter([
            {
                path: '/',
                errorElement: <ErrorPagina />,
                Component: () => {
                    throw new Error('TypeError: cannot read properties of undefined (stack...)')
                },
            },
        ])
        render(
            <QueryClientProvider client={crearQueryClient()}>
                <AuthProvider>
                    <RouterProvider router={router} />
                </AuthProvider>
            </QueryClientProvider>,
        )

        expect(
            await screen.findByRole('heading', { name: '¡Ups! Algo salió mal.' }),
        ).toBeInTheDocument()
        expect(screen.queryByText(/TypeError|stack/)).not.toBeInTheDocument()
        vi.restoreAllMocks()
    })

    it.each([
        [undefined, ['Eventos', 'Artistas', 'Sedes']],
        ['USER', ['Eventos', 'Artistas', 'Sedes', 'Mis tickets']],
        ['STAFF', ['Eventos', 'Artistas', 'Sedes', 'Validar ingreso', 'Tickets pagados']],
        [
            'ADMIN',
            ['Eventos', 'Artistas', 'Sedes', 'Validar ingreso', 'Tickets pagados', 'Administrar eventos'],
        ],
    ] as const)(
        'TEST-FE-GEN-003: la navbar de %s muestra solo sus enlaces',
        async (rol, esperados) => {
            renderConProveedores({ ruta: '/', rol })

            const nav = await screen.findByRole('navigation', { name: 'Principal' })
            if (rol) await within(nav).findByRole('link', { name: esperados.at(-1) })
            expect(enlacesDe(nav)).toEqual(esperados)
        },
    )

    it('TEST-FE-GEN-004: el menú móvil se abre con teclado y Escape lo cierra devolviendo el foco', async () => {
        const { user } = renderConProveedores({ ruta: '/' })

        const boton = await screen.findByRole('button', { name: 'Abrir menú' })
        boton.focus()
        await user.keyboard('{Enter}')

        const menu = screen.getByRole('navigation', { name: 'Menú móvil' })
        expect(screen.getByRole('button', { name: 'Cerrar menú' })).toHaveAttribute(
            'aria-expanded',
            'true',
        )
        await user.tab()
        expect(menu).toContainElement(document.activeElement as HTMLElement)

        await user.keyboard('{Escape}')
        expect(screen.queryByRole('navigation', { name: 'Menú móvil' })).not.toBeInTheDocument()
        expect(screen.getByRole('button', { name: 'Abrir menú' })).toHaveFocus()
    })

    it('el enlace "Saltar al contenido" apunta al main', async () => {
        renderConProveedores({ ruta: '/' })

        expect(await screen.findByRole('link', { name: 'Saltar al contenido' })).toHaveAttribute(
            'href',
            '#contenido',
        )
        expect(screen.getByRole('main')).toHaveAttribute('id', 'contenido')
    })
})
