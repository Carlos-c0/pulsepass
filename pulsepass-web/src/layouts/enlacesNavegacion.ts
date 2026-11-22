import type { Rol } from '../funcionalidades/auth/tipos'
import { RUTAS } from '../lib/rutas'

interface Enlace {
    a: string
    texto: string
    roles?: Rol[]
    // Para enlaces que tambien deben verse activos en sus subrutas
    prefijo?: string
}

// Tabla 8.4 del PRD: que ve cada rol en la navbar
const ENLACES: Enlace[] = [
    { a: RUTAS.eventos, texto: 'Eventos' },
    { a: RUTAS.artistas, texto: 'Artistas' },
    { a: RUTAS.sedes, texto: 'Sedes' },
    { a: RUTAS.misTickets, texto: 'Mis tickets', roles: ['USER'] },
    { a: RUTAS.validacion, texto: 'Validar ingreso', roles: ['STAFF', 'ADMIN'] },
    {
        a: RUTAS.ticketsPagadosInicio,
        texto: 'Tickets pagados',
        roles: ['STAFF', 'ADMIN'],
        prefijo: '/staff/eventos/',
    },
    { a: RUTAS.adminEventos, texto: 'Administrar eventos', roles: ['ADMIN'] },
]

export function enlacesPara(roles: Rol[]): Enlace[] {
    return ENLACES.filter((e) => !e.roles || e.roles.some((r) => roles.includes(r)))
}
