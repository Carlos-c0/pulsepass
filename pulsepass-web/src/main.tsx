import { QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { AvisosProvider } from './componentes/Avisos'
import { AuthProvider } from './funcionalidades/auth/AuthContext'
import './index.css'
import { crearQueryClient } from './lib/queryClient'
import { router } from './router'

const queryClient = crearQueryClient()

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <AuthProvider>
                <AvisosProvider>
                    <RouterProvider router={router} />
                </AvisosProvider>
            </AuthProvider>
        </QueryClientProvider>
    </StrictMode>,
)
