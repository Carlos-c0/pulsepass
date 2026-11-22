import { screen, waitFor } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import { guardarToken, obtenerToken } from '../../lib/sesionToken'
import { errorApi } from '../../test/mocks/datos'
import { peticionesA, server } from '../../test/mocks/server'
import { renderConProveedores } from '../../test/utils'

describe('Login', () => {
    it('TEST-FE-AUTH-001: login válido → sesión autenticada y vuelve a la ruta de origen', async () => {
        const { user, router } = renderConProveedores({ ruta: '/mis-tickets' })

        // RutaProtegida mando a /login recordando de donde venia
        await user.type(await screen.findByLabelText('Usuario o correo'), 'andrea')
        await user.type(screen.getByLabelText('Contraseña'), 'ClaveSegura2026!')
        await user.click(screen.getByRole('button', { name: 'Ingresar' }))

        expect(await screen.findByRole('heading', { name: 'Mis tickets' })).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/mis-tickets')
        expect(peticionesA('POST', '/api/auth/login')).toHaveLength(1)
        // AC-FE-009: el token queda solo en sessionStorage y el nombre del perfil aparece en la navbar
        expect(sessionStorage.getItem('pulsepass.sesion')).toContain('token-USER')
        expect(localStorage.getItem('pulsepass.sesion')).toBeNull()
        expect((await screen.findAllByText('Andrea')).length).toBeGreaterThan(0)
    })

    it('TEST-FE-AUTH-002: login 401 → "Credenciales inválidas" sin decir qué falló', async () => {
        server.use(
            http.post('*/api/auth/login', () =>
                HttpResponse.json(errorApi(401, 'Invalid credentials'), { status: 401 }),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/login' })

        await user.type(await screen.findByLabelText('Usuario o correo'), 'andrea')
        await user.type(screen.getByLabelText('Contraseña'), 'equivocada')
        await user.click(screen.getByRole('button', { name: 'Ingresar' }))

        expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales inválidas.')
    })

    it('TEST-FE-AUTH-003: login sin campos → errores y 0 peticiones', async () => {
        const { user } = renderConProveedores({ ruta: '/login' })

        await user.click(await screen.findByRole('button', { name: 'Ingresar' }))

        expect(await screen.findByText('Ingresa tu usuario o correo')).toBeInTheDocument()
        expect(screen.getByText('Ingresa tu contraseña')).toBeInTheDocument()
        expect(peticionesA('POST', '/api/auth/login')).toHaveLength(0)
    })
})

describe('Registro', () => {
    async function llenarRegistro(
        user: ReturnType<typeof renderConProveedores>['user'],
        password: string,
    ) {
        await user.type(await screen.findByLabelText('Nombre de usuario'), 'andrea')
        await user.type(screen.getByLabelText('Correo electrónico'), 'andrea@pulsepass.test')
        await user.type(screen.getByLabelText('Nombres'), 'Andrea')
        await user.type(screen.getByLabelText('Apellidos'), 'Ruiz')
        await user.type(screen.getByLabelText('Fecha de nacimiento'), '1998-04-12')
        await user.type(screen.getByLabelText('Contraseña (mín. 12 caracteres)'), password)
        await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))
    }

    it('TEST-FE-AUTH-004: contraseña de 11 caracteres → error y 0 peticiones', async () => {
        const { user } = renderConProveedores({ ruta: '/registro' })

        await llenarRegistro(user, 'Corta12345!')

        expect(
            await screen.findByText('La contraseña debe tener al menos 12 caracteres'),
        ).toBeInTheDocument()
        expect(peticionesA('POST', '/api/users')).toHaveLength(0)
    })

    it('TEST-FE-AUTH-005: registro 201 → va a /login con aviso de éxito', async () => {
        const { user, router } = renderConProveedores({ ruta: '/registro' })

        await llenarRegistro(user, 'ClaveSegura2026!')

        expect(await screen.findByText('Cuenta creada, inicia sesión.')).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/login')
    })

    it('TEST-FE-AUTH-006: registro 409 → mensaje genérico sin detalles internos', async () => {
        server.use(
            http.post('*/api/users', () =>
                HttpResponse.json(errorApi(409, 'Email already exists: andrea@pulsepass.test'), {
                    status: 409,
                }),
            ),
        )
        const { user } = renderConProveedores({ ruta: '/registro' })

        await llenarRegistro(user, 'ClaveSegura2026!')

        const aviso = await screen.findByRole('alert')
        expect(aviso).toHaveTextContent('El usuario o correo ya está registrado.')
        expect(aviso).not.toHaveTextContent('andrea@pulsepass.test')
    })
})

describe('Guardas de ruta', () => {
    it('TEST-FE-AUTH-007: RutaProtegida con anónimo → /login guardando "desde"', async () => {
        const { router } = renderConProveedores({ ruta: '/cuenta' })

        expect(await screen.findByRole('heading', { name: 'Inicia sesión' })).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/login')
        expect(router.state.location.state).toMatchObject({ desde: '/cuenta' })
    })

    it('TEST-FE-AUTH-008: USER en /admin/eventos → /403', async () => {
        const { router } = renderConProveedores({ ruta: '/admin/eventos', rol: 'USER' })

        expect(
            await screen.findByText('No tienes permisos para acceder a esta sección.'),
        ).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/403')
    })

    it('TEST-FE-AUTH-009: ADMIN en /admin/eventos → la página se muestra', async () => {
        renderConProveedores({ ruta: '/admin/eventos', rol: 'ADMIN' })

        expect(
            await screen.findByRole('heading', { name: 'Administración de eventos' }),
        ).toBeInTheDocument()
    })
})

describe('Sesión', () => {
    it('TEST-FE-AUTH-010: cerrar sesión borra el token y la caché', async () => {
        const { user, queryClient, router } = renderConProveedores({ ruta: '/cuenta', rol: 'USER' })

        await user.click(await screen.findByRole('button', { name: 'Cerrar sesión' }))

        expect(obtenerToken()).toBeNull()
        expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
        // El perfil cargado en /cuenta ya no esta en cache (en / se cargan otras consultas publicas)
        expect(queryClient.getQueryData(['usuario', 'andrea@pulsepass.test'])).toBeUndefined()
        await waitFor(() => expect(router.state.location.pathname).toBe('/'))
    })

    it('TEST-FE-AUTH-011: un token vencido al restaurar → sesión anónima sin llamar a /me', async () => {
        guardarToken('token-USER', 900, Date.now() - 2 * 900 * 1000)

        renderConProveedores({ ruta: '/' })

        expect(await screen.findAllByRole('link', { name: 'Ingresar' })).not.toHaveLength(0)
        expect(peticionesA('GET', '/api/auth/me')).toHaveLength(0)
        expect(sessionStorage.getItem('pulsepass.sesion')).toBeNull()
    })

    it('AC-FE-019: un 401 con sesión cierra la sesión y lleva a /login con "Tu sesión expiró"', async () => {
        server.use(
            http.get('*/api/tickets/by-user', () =>
                HttpResponse.json(errorApi(401, 'Token expired'), { status: 401 }),
            ),
        )
        const { router } = renderConProveedores({ ruta: '/mis-tickets', rol: 'USER' })

        expect(await screen.findByText('Tu sesión expiró. Inicia sesión de nuevo.')).toBeInTheDocument()
        expect(router.state.location.pathname).toBe('/login')
        expect(obtenerToken()).toBeNull()
    })
})
