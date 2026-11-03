# Minicash

Aplicación Android de finanzas personales, pensada para llevar el dinero del día a día en pareja o en lo individual. Todo vive en el dispositivo: sin cuentas, sin servidores y sin enviar datos a ningún lado.

Hecha con Kotlin, Jetpack Compose y Material 3 Expressive.

## Qué hace

- **Inicio.** Patrimonio total, tus cuentas y la distribución de gastos e ingresos por categoría. Un selector de mes permite revisar meses anteriores; por defecto muestra el mes en curso.
- **Historial.** Gastos, ingresos y transferencias entre cuentas, organizados por mes.
- **Gastos fijos.** Presupuesto por quincenas: partidas de ingreso, gasto y reembolso en cuadrícula, con resumen de lo disponible y barra de avance. Se marcan como ejecutadas, se reordenan con pulsación larga y se pueden copiar de una quincena a otra.
- **Créditos.** Compras a meses con avance por mensualidad, fecha estimada de término y aviso antes de cada pago. El día de pago se configura en la institución (tarjeta o banco) y las compras lo heredan; cada compra puede usar otro día.
- **Reportes.** Ingresos contra gastos por mes (6 o 12 meses), con promedios y desglose mensual.
- **Copias de seguridad.** Exportación e importación en JSON, con copia automática opcional.
- **Personalización.** Tema claro, oscuro o AMOLED, color dinámico o de acento, barra de navegación integrada o flotante (se oculta al desplazar) y módulos opcionales de Gastos fijos y Créditos.

## Privacidad

Los datos se guardan únicamente en una base de datos local (Room) del dispositivo. La app no pide cuenta, no usa red para tus finanzas y las copias de seguridad las guardas tú donde prefieras.

## Instalación

1. Descarga el APK más reciente de la sección de *Releases* (o compílalo tú, ver más abajo).
2. Ábrelo en tu teléfono y permite la instalación desde ese origen si Android lo pide.
3. En la primera apertura, un asistente de bienvenida te guía para empezar.

Requiere Android 8.0 (API 26) o superior.

## Compilar

Necesitas Android Studio reciente o el JDK y el SDK de Android configurados.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:installDebug
./gradlew :app:assembleRelease
```

En Windows usa `gradlew.bat`. El APK release queda en `app/build/outputs/apk/release/`.

La firma de release usa `~/.android/debug.keystore`. Si compilas en otra computadora, copia ese archivo a la misma ruta antes del primer build; así el APK sigue instalándose como actualización de las versiones anteriores.

## Tecnología

| Área | Elección |
|---|---|
| Lenguaje y UI | Kotlin, Jetpack Compose, Material 3 Expressive |
| Datos | Room (con migraciones y esquemas exportados en `app/schemas`) |
| Ajustes | DataStore |
| Tareas en segundo plano | WorkManager (avisos de pago y copias automáticas) |
| Respaldos | Gson, formato JSON versionado |
| Reordenar listas | `sh.calvin.reorderable` |

## Estructura

```
app/src/main/java/com/uramos/minicash/
├── data/       base de datos, entidades, respaldos, preferencias y workers
├── ui/
│   ├── components/   botones, tarjetas, diálogos y gráficas reutilizables
│   ├── navigation/   pantalla principal y barra de navegación
│   ├── screens/      inicio, historial, gastos fijos, créditos, reportes y ajustes
│   └── theme/        colores, tipografía, formas y movimiento
└── MainActivity.kt
```

Más detalle de diseño y contratos entre capas en [DESIGN.md](DESIGN.md) y [CONTRACTS.md](CONTRACTS.md).

## Pruebas

Las pruebas unitarias cubren el cálculo de fechas de pago y la herencia entre institución y compra, el teclado numérico y el formato compacto de montos, y el contraste de colores.

```bash
./gradlew :app:testDebugUnitTest
```

## Estado

Proyecto personal en uso diario. Está en versión 1.0.
