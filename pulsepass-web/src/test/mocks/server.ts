import { setupServer } from 'msw/node'
import { handlers } from './handlers'

export const server = setupServer(...handlers)

// Registro de las peticiones que salieron, para comprobar que un formulario invalido no llama a la API
export const peticiones: Request[] = []
server.events.on('request:start', ({ request }) => {
    peticiones.push(request)
})

export const peticionesA = (metodo: string, fragmento: string) =>
    peticiones.filter((p) => p.method === metodo && new URL(p.url).pathname.includes(fragmento))
