import type { ComponentProps, ComponentType } from 'react'
import {
    IconoCultura,
    IconoDeporte,
    IconoEducacion,
    IconoEntretenimiento,
    IconoMusica,
    IconoTecnologia,
} from '../../../componentes/Iconos'
import type { CategoriaEvento } from '../tipos'

interface EstiloCategoria {
    fondo: string
    texto: string
    Icono: ComponentType<ComponentProps<'svg'>>
}

// La API no trae imagenes: cada categoria tiene su panel de color e icono (DES-02).
// Clases completas, en claro y en oscuro, para que Tailwind las genere.
export const ESTILO_CATEGORIA: Record<CategoriaEvento, EstiloCategoria> = {
    MUSIC: {
        fondo: 'bg-indigo-100 dark:bg-indigo-950',
        texto: 'text-indigo-800 dark:text-indigo-300',
        Icono: IconoMusica,
    },
    TECHNOLOGY: {
        fondo: 'bg-sky-100 dark:bg-sky-950',
        texto: 'text-sky-800 dark:text-sky-300',
        Icono: IconoTecnologia,
    },
    SPORTS: {
        fondo: 'bg-emerald-100 dark:bg-emerald-950',
        texto: 'text-emerald-800 dark:text-emerald-300',
        Icono: IconoDeporte,
    },
    CULTURE: {
        fondo: 'bg-rose-100 dark:bg-rose-950',
        texto: 'text-rose-800 dark:text-rose-300',
        Icono: IconoCultura,
    },
    EDUCATION: {
        fondo: 'bg-amber-100 dark:bg-amber-950',
        texto: 'text-amber-800 dark:text-amber-300',
        Icono: IconoEducacion,
    },
    ENTERTAINMENT: {
        fondo: 'bg-violet-100 dark:bg-violet-950',
        texto: 'text-violet-800 dark:text-violet-300',
        Icono: IconoEntretenimiento,
    },
}
