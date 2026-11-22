import type { ReactNode } from 'react'

// Clases compartidas por varios componentes. Viven aparte porque un archivo de componentes
// solo debe exportar componentes (asi funciona la recarga en caliente de Vite).

export type Variante =
    'primario' | 'secundario' | 'peligro' | 'fantasma' | 'blanco' | 'contornoBlanco'
export type Tamano = 'normal' | 'grande' | 'pequeno'

// Clases completas por variante: Tailwind solo genera las clases que encuentra escritas (FE-008)
export const VARIANTES: Record<Variante, string> = {
    primario: 'bg-marca-600 text-white hover:bg-marca-700',
    secundario:
        'border border-slate-300 bg-white text-slate-900 hover:bg-slate-50 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 dark:hover:bg-slate-800',
    peligro: 'bg-red-600 text-white hover:bg-red-700',
    fantasma: 'text-marca-700 hover:bg-marca-50 dark:text-indigo-300 dark:hover:bg-slate-800',
    // Sobre el degradado de Inicio: se ven igual en claro y en oscuro (sin variantes dark:)
    blanco: 'bg-white text-marca-700 hover:bg-marca-50',
    contornoBlanco: 'border border-white/90 text-white hover:bg-white/10',
}

export const TAMANOS: Record<Tamano, string> = {
    pequeno: 'min-h-9 px-4 text-sm',
    normal: 'min-h-11 px-5 text-[15px]',
    grande: 'min-h-13 px-6 text-base',
}

export const estiloBoton =
    'inline-flex items-center justify-center gap-2 rounded-lg font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-marca-600 disabled:cursor-not-allowed'

export const estilosControl =
    'block w-full min-h-11 rounded-lg border bg-white px-3.5 py-2 text-base text-slate-900 placeholder:text-slate-400 focus-visible:outline-2 focus-visible:outline-offset-1 focus-visible:outline-marca-600 dark:bg-slate-900 dark:text-slate-100 dark:placeholder:text-slate-500'

export const bordeControl = (error?: string) =>
    error ? 'border-red-600 dark:border-red-500' : 'border-slate-300 dark:border-slate-700'

export function describirCampo(idCampo: string, error?: string, ayuda?: ReactNode) {
    if (error) return `${idCampo}-error`
    if (ayuda) return `${idCampo}-ayuda`
    return undefined
}