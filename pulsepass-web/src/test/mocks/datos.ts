// Datos del escenario de referencia del PRD (seccion 22)
export const sede = {
    id: 1,
    code: 'VEN-SMR-01',
    name: 'Marina Convention Center',
    city: 'Santa Marta',
    address: 'Calle 1 # 2-3',
    capacity: 3,
    active: true,
}

export const artistas = [
    { id: 1, stageName: 'Solar Beat', country: 'Colombia', genre: 'Electrónica', active: true },
    { id: 2, stageName: 'Neon Waves', country: 'Colombia', genre: 'Synthpop', active: true },
    { id: 3, stageName: 'Caribbean Sound', country: 'Colombia', genre: 'Champeta', active: true },
]

const [solarBeat, neonWaves] = artistas as [(typeof artistas)[number], (typeof artistas)[number]]

export const eventoCMF = {
    id: 10,
    eventCode: 'CMF-2026',
    name: 'Caribbean Music Fest 2026',
    description: 'Tres escenarios frente al mar.',
    category: 'MUSIC',
    status: 'PUBLISHED',
    eventDate: '2026-12-12T20:00:00',
    minimumAge: 18,
    venueCode: 'VEN-SMR-01',
    venueName: 'Marina Convention Center',
    artists: [solarBeat, neonWaves],
}

export const resumenCMF = {
    id: 10,
    eventCode: 'CMF-2026',
    name: 'Caribbean Music Fest 2026',
    category: 'MUSIC',
    status: 'PUBLISHED',
    eventDate: '2026-12-12T20:00:00',
    venueName: 'Marina Convention Center',
    venueCity: 'Santa Marta',
}

export const resumenTech = {
    id: 11,
    eventCode: 'TCH-2026',
    name: 'Tech Summit Caribe',
    category: 'TECHNOLOGY',
    status: 'PUBLISHED',
    eventDate: '2026-11-30T09:00:00',
    venueName: 'Marina Convention Center',
    venueCity: 'Santa Marta',
}

export const usuarios = {
    USER: { id: 1, username: 'andrea', email: 'andrea@pulsepass.test', roles: ['USER'] },
    STAFF: { id: 2, username: 'laura', email: 'laura@pulsepass.test', roles: ['STAFF'] },
    ADMIN: { id: 3, username: 'admin', email: 'admin@pulsepass.test', roles: ['ADMIN'] },
} as const

export const perfilAndrea = {
    id: 1,
    username: 'andrea',
    email: 'andrea@pulsepass.test',
    active: true,
    firstName: 'Andrea',
    lastName: 'Ruiz',
    phone: null,
    city: 'Santa Marta',
    birthDate: '1998-04-12',
}

export function ticket(cambios: Partial<typeof ticketBase> = {}) {
    return { ...ticketBase, ...cambios }
}

const ticketBase = {
    id: 100,
    ticketCode: 'TKT-8F2A1C',
    type: 'VIP',
    price: 250000,
    status: 'PAID',
    purchaseDate: '2026-11-05T14:32:00',
    userEmail: 'andrea@pulsepass.test',
    eventCode: 'CMF-2026',
    eventName: 'Caribbean Music Fest 2026',
}

export function errorApi(status: number, message: string, details: Record<string, string> = {}) {
    const nombres: Record<number, string> = {
        400: 'Bad Request',
        401: 'Unauthorized',
        403: 'Forbidden',
        404: 'Not Found',
        409: 'Conflict',
        500: 'Internal Server Error',
    }
    return {
        timestamp: '2026-11-08T10:00:00',
        status,
        error: nombres[status] ?? 'Error',
        message,
        details,
    }
}
