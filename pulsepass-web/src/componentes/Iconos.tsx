import type { ComponentProps } from 'react'

// Iconos SVG de trazo (DES-01). Son decorativos: aria-hidden por defecto.
type PropsIcono = ComponentProps<'svg'>

function Base({ children, className = 'size-5', ...props }: PropsIcono) {
    return (
        <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth={1.8}
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
            className={className}
            {...props}
        >
            {children}
        </svg>
    )
}

// Categorias de evento (DES-02)
export const IconoMusica = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M9 18V5l12-2v13" />
        <circle cx="6" cy="18" r="3" />
        <circle cx="18" cy="16" r="3" />
    </Base>
)
export const IconoTecnologia = (p: PropsIcono) => (
    <Base {...p}>
        <rect x="3" y="4" width="18" height="12" rx="2" />
        <path d="M12 16v4M8 20h8" />
    </Base>
)
export const IconoDeporte = (p: PropsIcono) => (
    <Base {...p}>
        <circle cx="12" cy="12" r="9" />
        <path d="M12 3v18M3 12h18" />
    </Base>
)
export const IconoCultura = (p: PropsIcono) => (
    <Base {...p}>
        <path d="m3 9 9-6 9 6M4 21h16M5 9v12M19 9v12M9.5 12v6M14.5 12v6" />
    </Base>
)
export const IconoEducacion = (p: PropsIcono) => (
    <Base {...p}>
        <path d="m2 10 10-5 10 5-10 5z" />
        <path d="M6 12v4c3 2.5 9 2.5 12 0v-4" />
    </Base>
)
export const IconoEntretenimiento = (p: PropsIcono) => (
    <Base {...p}>
        <path d="m12 2.8 2.8 5.8 6.3.9-4.6 4.4 1.1 6.3L12 17.2l-5.6 3 1.1-6.3-4.6-4.4 6.3-.9z" />
    </Base>
)

// Interfaz
export const IconoCalendario = (p: PropsIcono) => (
    <Base {...p}>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M16 3v4M8 3v4M3 11h18" />
    </Base>
)
export const IconoUbicacion = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M12 21s-7-6.2-7-11.5a7 7 0 0 1 14 0C19 14.8 12 21 12 21z" />
        <circle cx="12" cy="9.5" r="2.5" />
    </Base>
)
export const IconoUsuario = (p: PropsIcono) => (
    <Base {...p}>
        <circle cx="12" cy="8" r="4" />
        <path d="M4 21a8 8 0 0 1 16 0" />
    </Base>
)
export const IconoSede = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M4 21V9l8-6 8 6v12" />
        <path d="M9 21v-6h6v6" />
    </Base>
)
export const IconoTicket = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M3 8a2 2 0 0 0 2-2h14a2 2 0 0 0 2 2v2a2 2 0 0 0 0 4v2a2 2 0 0 0-2 2H5a2 2 0 0 0-2-2v-2a2 2 0 0 0 0-4z" />
        <path d="M13 6v2M13 11v2M13 16v2" />
    </Base>
)
export const IconoAlerta = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
        <path d="M12 9v4M12 17h.01" />
    </Base>
)
export const IconoCheck = (p: PropsIcono) => (
    <Base {...p}>
        <path d="m5 12 5 5L20 7" />
    </Base>
)
export const IconoRayo = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M13 2 4 14h7l-1 8 9-12h-7z" />
    </Base>
)
export const IconoBandeja = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M22 12h-6l-2 3h-4l-2-3H2" />
        <path d="M5.5 5h13L22 12v6a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2v-6z" />
    </Base>
)
export const IconoCerrar = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M18 6 6 18M6 6l12 12" />
    </Base>
)
export const IconoMenu = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M4 6h16M4 12h16M4 18h16" />
    </Base>
)
export const IconoBuscar = (p: PropsIcono) => (
    <Base {...p}>
        <circle cx="11" cy="11" r="7" />
        <path d="m20 20-3.5-3.5" />
    </Base>
)
export const IconoFlechaIzquierda = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M19 12H5M12 19l-7-7 7-7" />
    </Base>
)
export const IconoFlechaDerecha = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M5 12h14M12 5l7 7-7 7" />
    </Base>
)
export const IconoChevron = (p: PropsIcono) => (
    <Base {...p}>
        <path d="m6 9 6 6 6-6" />
    </Base>
)
export const IconoMas = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M12 5v14M5 12h14" />
    </Base>
)
export const IconoCopiar = (p: PropsIcono) => (
    <Base {...p}>
        <rect x="9" y="9" width="12" height="12" rx="2" />
        <path d="M5 15H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v1" />
    </Base>
)
export const IconoOjo = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" />
        <circle cx="12" cy="12" r="3" />
    </Base>
)
export const IconoOjoTachado = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M10.6 5.1A10 10 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-2.3 3.2M6.6 6.6C3.6 8.5 2 12 2 12s3.5 7 10 7a9.6 9.6 0 0 0 5.4-1.6" />
        <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2M2 2l20 20" />
    </Base>
)
export const IconoLuna = (p: PropsIcono) => (
    <Base {...p}>
        <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z" />
    </Base>
)
export const IconoSol = (p: PropsIcono) => (
    <Base {...p}>
        <circle cx="12" cy="12" r="4" />
        <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" />
    </Base>
)

// Logo: cuadro de marca con la nota musical en blanco
export function Logo({ className = 'size-8' }: { className?: string }) {
    return (
        <span
            aria-hidden="true"
            className={`bg-marca-600 grid shrink-0 place-items-center rounded-lg text-white ${className}`}
        >
      <IconoMusica className="size-[55%]" />
    </span>
    )
}
