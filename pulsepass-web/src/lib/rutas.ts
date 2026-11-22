// Todas las rutas de la aplicacion en un solo lugar (FE-003).
// Las que llevan parametro son funciones para no armar strings a mano en cada pagina.
export const RUTAS = {
    inicio: '/',
    eventos: '/eventos',
    evento: (eventCode: string) => `/eventos/${encodeURIComponent(eventCode)}`,
    artistas: '/artistas',
    artista: (id: number) => `/artistas/${id}`,
    sedes: '/sedes',
    sede: (code: string) => `/sedes/${encodeURIComponent(code)}`,
    login: '/login',
    registro: '/registro',
    cuenta: '/cuenta',
    misTickets: '/mis-tickets',
    ticket: (ticketCode: string) => `/mis-tickets/${encodeURIComponent(ticketCode)}`,
    confirmacion: (ticketCode: string) => `/compra/confirmacion/${encodeURIComponent(ticketCode)}`,
    validacion: '/staff/validacion',
    ticketsPagadosInicio: '/staff/eventos/tickets',
    ticketsPagados: (eventCode: string) => `/staff/eventos/${encodeURIComponent(eventCode)}/tickets`,
    adminEventos: '/admin/eventos',
    adminNuevoEvento: '/admin/eventos/nuevo',
    adminEvento: (eventCode: string) => `/admin/eventos/${encodeURIComponent(eventCode)}`,
    sinPermisos: '/403',
} as const
