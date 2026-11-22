import clsx from 'clsx'

const SEGMENTOS = 8

// Solo orienta a la persona: la regla real es la del formulario (12 caracteres, 72 bytes).
// Suma puntos por largo y por variedad de caracteres, de 0 a 8.
function puntaje(password: string): number {
    const variedad = [/[a-z]/, /[A-Z]/, /\d/, /[^A-Za-z0-9]/].filter((r) => r.test(password)).length
    const porLargo =
        password.length >= 20 ? 4 : password.length >= 16 ? 3 : password.length >= 12 ? 2 : 1
    return Math.min(SEGMENTOS, porLargo + variedad)
}

function nivel(puntos: number): string {
    if (puntos >= 7) return 'fuerte'
    if (puntos >= 4) return 'media'
    return 'débil'
}

export default function MedidorFortaleza({ password }: { password: string }) {
    const puntos = password ? puntaje(password) : 0
    return (
        <div className="space-y-1.5">
            <div className="flex gap-1.5" aria-hidden="true">
                {Array.from({ length: SEGMENTOS }, (_, i) => (
                    <span
                        key={i}
                        className={clsx(
                            'h-1.5 flex-1 rounded-full',
                            i < puntos ? 'bg-marca-600' : 'bg-slate-300 dark:bg-slate-700',
                        )}
                    />
                ))}
            </div>
            <p className="text-[13px] text-slate-600 dark:text-slate-400">
                Fortaleza: {password ? nivel(puntos) : '—'}
            </p>
        </div>
    )
}
