import Boton from '../../../componentes/Boton'
import Campo from '../../../componentes/Campo'
import { IconoBuscar } from '../../../componentes/Iconos'
import Selector from '../../../componentes/Selector'
import type { Artista } from '../../artistas/tipos'
import { EventCategorySchema } from '../tipos'

export interface Filtros {
    q: string
    categoria: string
    artista: string
}

interface BarraFiltrosProps {
    filtros: Filtros
    artistas: Artista[]
    onCambiar: (cambio: Partial<Filtros>) => void
    onLimpiar: () => void
}

export default function BarraFiltros({
                                         filtros,
                                         artistas,
                                         onCambiar,
                                         onLimpiar,
                                     }: BarraFiltrosProps) {
    return (
        <form
            role="search"
            onSubmit={(e) => e.preventDefault()}
            className="grid gap-3 sm:grid-cols-2 lg:grid-cols-[3fr_2fr_2fr_auto] lg:items-end"
        >
            <Campo
                etiqueta="Buscar por nombre"
                type="search"
                value={filtros.q}
                placeholder="Buscar por nombre..."
                icono={<IconoBuscar className="size-[18px]" />}
                onChange={(e) => onCambiar({ q: e.target.value })}
            />
            <Selector
                etiqueta="Categoría"
                value={filtros.categoria}
                onChange={(e) => onCambiar({ categoria: e.target.value })}
            >
                <option value="">Todas</option>
                {EventCategorySchema.options.map((c) => (
                    <option key={c} value={c}>
                        {c}
                    </option>
                ))}
            </Selector>
            <Selector
                etiqueta="Artista"
                value={filtros.artista}
                onChange={(e) => onCambiar({ artista: e.target.value })}
            >
                <option value="">Todos</option>
                {artistas.map((a) => (
                    <option key={a.id} value={a.stageName}>
                        {a.stageName}
                    </option>
                ))}
            </Selector>
            <Boton variante="secundario" onClick={onLimpiar}>
                Limpiar
            </Boton>
        </form>
    )
}
