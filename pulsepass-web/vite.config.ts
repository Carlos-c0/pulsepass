/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
    plugins: [react(), tailwindcss()],
    server: {
        // En desarrollo, /api se reenvia al backend de Spring Boot (asi no hace falta CORS)
        proxy: { '/api': 'http://localhost:8080' },
    },
    test: {
        environment: 'jsdom',
        setupFiles: ['./src/test/setup.ts'],
        css: false,
    },
})
