import type { ComponentType } from 'react'
import { createBrowserRouter, type RouteObject } from 'react-router'
import Layout from './layouts/Layout'
import RutaPorRol from './layouts/RutaPorRol'
import RutaProtegida from './layouts/RutaProtegida'
import SoloAnonimo from './layouts/SoloAnonimo'
import Cargando from './componentes/Cargando'
import ErrorPagina from './pages/ErrorPagina'
import Inicio from './pages/Inicio'

// Cada pagina (salvo Layout e Inicio) se descarga solo cuando se visita (FE-012)
const pagina = (cargar: () => Promise<{ default: ComponentType }>) => async () => ({
    Component: (await cargar()).default,
})

export const rutas: RouteObject[] = [
    {
        path: '/',
        Component: Layout,
        errorElement: <ErrorPagina />,
        // Se ve mientras llega la primera pagina lazy (si se entra directo a /eventos, por ejemplo)
        hydrateFallbackElement: <Cargando />,
        children: [
            { index: true, Component: Inicio },
            { path: 'eventos', lazy: pagina(() => import('./pages/CatalogoEventos')) },
            { path: 'eventos/:eventCode', lazy: pagina(() => import('./pages/DetalleEvento')) },
            { path: 'artistas', lazy: pagina(() => import('./pages/ListaArtistas')) },
            { path: 'artistas/:id', lazy: pagina(() => import('./pages/DetalleArtista')) },
            { path: 'sedes', lazy: pagina(() => import('./pages/ListaSedes')) },
            { path: 'sedes/:code', lazy: pagina(() => import('./pages/DetalleSede')) },
            {
                Component: SoloAnonimo,
                children: [
                    { path: 'login', lazy: pagina(() => import('./pages/Login')) },
                    { path: 'registro', lazy: pagina(() => import('./pages/Registro')) },
                ],
            },
            {
                Component: RutaProtegida,
                children: [
                    { path: 'cuenta', lazy: pagina(() => import('./pages/Cuenta')) },
                    {
                        element: <RutaPorRol roles={['USER']} />,
                        children: [
                            { path: 'mis-tickets', lazy: pagina(() => import('./pages/MisTickets')) },
                            {
                                path: 'mis-tickets/:ticketCode',
                                lazy: pagina(() => import('./pages/DetalleTicket')),
                            },
                            {
                                path: 'compra/confirmacion/:ticketCode',
                                lazy: pagina(() => import('./pages/ConfirmacionCompra')),
                            },
                        ],
                    },
                    {
                        element: <RutaPorRol roles={['STAFF', 'ADMIN']} />,
                        children: [
                            { path: 'staff/validacion', lazy: pagina(() => import('./pages/ValidacionIngreso')) },
                            {
                                // El codigo es opcional: el enlace "Tickets pagados" de la navbar llega sin evento elegido
                                path: 'staff/eventos/:eventCode?/tickets',
                                lazy: pagina(() => import('./pages/TicketsPagadosEvento')),
                            },
                        ],
                    },
                    {
                        element: <RutaPorRol roles={['ADMIN']} />,
                        children: [
                            { path: 'admin/eventos', lazy: pagina(() => import('./pages/AdminEventos')) },
                            {
                                path: 'admin/eventos/nuevo',
                                lazy: pagina(() => import('./pages/AdminCrearEvento')),
                            },
                            {
                                path: 'admin/eventos/:eventCode',
                                lazy: pagina(() => import('./pages/AdminGestionEvento')),
                            },
                        ],
                    },
                ],
            },
            { path: '403', lazy: pagina(() => import('./pages/SinPermisos')) },
            { path: '*', lazy: pagina(() => import('./pages/NoEncontrada')) },
        ],
    },
]

export const router = createBrowserRouter(rutas)