import js from '@eslint/js'
import reactHooks from 'eslint-plugin-react-hooks'
import { reactRefresh } from 'eslint-plugin-react-refresh'
import { defineConfig, globalIgnores } from 'eslint/config'
import globals from 'globals'
import tseslint from 'typescript-eslint'

export default defineConfig([
    globalIgnores(['dist', 'coverage']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            js.configs.recommended,
            tseslint.configs.recommended,
            reactHooks.configs.flat.recommended,
            reactRefresh.configs.vite(),
        ],
        languageOptions: {
            ecmaVersion: 2023,
            globals: globals.browser,
        },
        rules: {
            '@typescript-eslint/no-explicit-any': 'error',
            'no-restricted-imports': [
                'error',
                { paths: [{ name: 'react-router-dom', message: 'En React Router v8 se importa de "react-router".' }] },
            ],
        },
    },
    {
        // Los archivos de contexto, pruebas y router exportan algo mas que componentes
        files: ['src/test/**', '**/*.test.{ts,tsx}', 'src/router.tsx', 'src/**/*Context.tsx', 'src/componentes/Avisos.tsx'],
        rules: { 'react-refresh/only-export-components': 'off' },
    },
])
