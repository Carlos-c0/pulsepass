import PaginaCodigo from '../componentes/PaginaCodigo'

interface NoEncontradaProps {
    titulo?: string
}

// Tambien la usan los detalles cuando la API responde 404
export default function NoEncontrada({
                                         titulo = 'No encontramos esta página.',
                                     }: NoEncontradaProps) {
    return <PaginaCodigo codigo="404" mensaje={titulo} />
}
