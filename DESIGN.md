# Guía de Diseño y Sistema de Tokens - Minicash (Material 3 Expressive)

Este documento establece las especificaciones inmutables de UI, movimiento y tokens para **Minicash**, gobernado por **Material 3 Expressive** y Jetpack Compose.

> **Convención:** todo lo marcado como **NO VERIFICADO** es una propuesta que aún no ha compilado en este proyecto. Se confirma con el compilador en la fase indicada antes de usarse; hasta entonces no debe darse por existente.

---

## 1. Jerarquía de Contenedores Tonales y Superficies

Minicash estructura la profundidad visual mediante los niveles tonales de superficie de Material 3:

| Nivel de Superficie | Rol en Minicash | Token de Forma Asociado |
| :--- | :--- | :--- |
| **`surfaceContainerLowest`** | Fondo general de pantalla y Scaffolds | Plano (`0.dp`) |
| **`surfaceContainerLow`** | Agrupadores grandes, fondos de sección y Donut Chart | `MinicashShapes.ContainerLarge` (24.dp) |
| **`surfaceContainer`** | Tarjetas de contenido estándar y modales (`BottomSheet`) | `MinicashShapes.Card` (20.dp) |
| **`surfaceContainerHigh`** | Filas interactivas, chips inactivos y cápsula flotante | `MinicashShapes.Card` (20.dp) o `Pill` |
| **`surfaceContainerHighest`** | Destacados, botones activos y micro-badges | `Pill` o `MinicashShapes.Hero` (28.dp) |

> **Regla de neutros teñidos:** los neutros de superficie no deben ser grises puros monocromáticos; deben conservar un matiz sutil derivado de la tonalidad primaria esmeralda.

---

## 2. Tokens de Forma Propios (MinicashShapes)

Para erradicar colisiones con los nombres estándar de Material 3 (donde `medium` equivale a 12.dp), Minicash define su propia escala nominal:

| Token de Forma | Radio / Geometría | Uso Específico |
| :--- | :--- | :--- |
| **`Sharp`** | `RoundedCornerShape(4.dp)` | Esquinas intermedias o de conexión en listas segmentadas |
| **`Element`** | `RoundedCornerShape(8.dp)` | Botones de calculadora y selectores compactos |
| **`Input`** | `RoundedCornerShape(12.dp)` | Campos de texto y áreas de entrada de datos |
| **`Compact`** | `RoundedCornerShape(16.dp)` | Chips y tarjetas pequeñas de baja jerarquía |
| **`Card`** | `RoundedCornerShape(20.dp)` | Tarjetas interactivas estándar de listas y transacciones |
| **`ContainerLarge`** | `RoundedCornerShape(24.dp)` | Agrupadores principales, tarjetas de resumen y Donut |
| **`Hero`** | `RoundedCornerShape(28.dp)` | Contenedores principales de cabecera en Inicio |
| **`Pill`** | `CircleShape` | Botones de acción, chips de filtro y pestañas activas |
| **`FABShape`** | `RoundedCornerShape(18.dp)` | Botón de acción flotante (FAB) cuadrado suavizado (ver sección 11) |

### Contraste de formas y listas segmentadas

- **Contraste de formas:** el elemento protagonista de una pantalla se distingue tanto por escala como por una curvatura más pronunciada respecto a las tarjetas secundarias.
- **Listas segmentadas:** todo grupo continuo (movimientos por día, partidas presupuestales, cuentas, créditos) utiliza el helper `rememberSegmentedShape`:
  - **Elemento único (`count == 1`):** 4 esquinas con `ContainerLarge` (24.dp).
  - **Primer elemento (`index == 0`):** esquinas superiores `ContainerLarge` (24.dp), inferiores `Sharp` (4.dp).
  - **Elementos intermedios:** las 4 esquinas `Sharp` (4.dp).
  - **Último elemento (`index == count - 1`):** esquinas superiores `Sharp` (4.dp), inferiores `ContainerLarge` (24.dp).
  - **Reinicio por grupo:** cada grupo (por ejemplo, cada fecha en Historial) reinicia el conteo de forma independiente.
- **Morphing:** al interactuar o seleccionar botones principales, la forma anima sus radios mediante resortes gobernados por el esquema de movimiento. Prohibido hardcodear radios fijos en composables.

---

## 3. Escala de Tokens de Espaciado (Spacing Tokens)

Se accede a la escala de espaciado centralizada mediante `MinicashThemeExtensions.spacing` (`LocalSpacing`):

| Token | Valor (dp) | Uso Sugerido |
| :--- | :--- | :--- |
| **`none`** | `0.dp` | Sin separación |
| **`extraSmall`** | `4.dp` | Separación mínima entre filas segmentadas e iconos |
| **`small`** | `8.dp` | Espaciado entre elementos de un mismo bloque o padding compacto |
| **`medium`** | `12.dp` | Margen interno de tarjetas y separación entre ítems |
| **`standard`** | `16.dp` | Padding general de pantalla y márgenes laterales |
| **`large`** | `20.dp` | Separación entre secciones medianas |
| **`extraLarge`** | `24.dp` | Separación entre bloques principales |
| **`hero`** | `32.dp` | Separación entre secciones mayores de una pantalla |

### Ritmo y espacio en pantalla

- Margen lateral: `spacing.standard` (16.dp).
- Separación entre secciones mayores: `spacing.hero` (32.dp).
- Los títulos de sección se ubican **fuera de las tarjetas**, con el estilo "título de sección" (ver sección 8) y una separación de `spacing.medium` (12.dp) hacia su contenido.
- El aire separa: no se usan divisores pesados ni sombras para delimitar bloques.

---

## 4. Esquema de Movimiento (Motion Physics)

Todas las animaciones de estado y transición se rigen por el esquema de movimiento del tema (`MaterialTheme.motionScheme`). Quedan erradicados los valores de resorte o interpoladores sueltos.

### Familias y velocidades

1. **Espacial (Spatial Specs):** movimiento físico con rebote elástico sutil para cambios de geometría, expansión horizontal de la barra flotante, reposición del FAB y desplazamiento de modales.
   - *Verificado en compilador:* `MaterialTheme.motionScheme.defaultSpatialSpec`.
   - *NO VERIFICADO (se comprueba en Fase 1):* `fastSpatialSpec`, `slowSpatialSpec`.
2. **Efectos (Effects Specs):** movimiento sin rebote para transiciones de alfa, desvanecimientos, opacidades y color.
   - *NO VERIFICADO (se comprueba en Fase 1):* `fastEffectsSpec`, `defaultEffectsSpec`, `slowEffectsSpec`.

### Momentos expresivos vs. flujos utilitarios

- **Expresivo (resortes):** selección y cambio de destino en la barra flotante, morphing al pulsar botones principales, aparición del FAB contextual y asentamiento del odómetro numérico.
- **Estándar (sin rebote):** flujos utilitarios de entrada de texto, teclado numérico, scroll continuo y validaciones de formulario.
- **Seguimiento del dedo:** las transformaciones reactivas al arrastre directo (como el deslizamiento del `HorizontalPager` con `graphicsLayer`) siguen el movimiento del dedo; la física de resorte solo interviene al soltar, para el asentamiento final.
- **Prohibición:** no se usan curvas `tween` ni interpoladores lineales arbitrarios salvo justificación técnica documentada.

### Reducción de movimiento y accesibilidad

La app lee `Settings.Global.ANIMATOR_DURATION_SCALE` **una sola vez a nivel de tema** (con `remember` u observador reactivo, nunca por composable ni en cada recomposición) y lo expone mediante el CompositionLocal `LocalAnimationRestriction`. Solo hay dos estados:

- **Escala == 0f:** corte instantáneo (`snap`). Se suprimen todas las animaciones.
- **Cualquier otro valor:** comportamiento normal con los specs del `motionScheme`.

> **NO VERIFICADO (Fase 1):** si Compose ya respeta por sí solo una escala de animación en 0 en las animaciones basadas en física. Se verifica antes de añadir lógica propia, para no duplicarla.

---

## 5. Retroalimentación Háptica

Se provee mediante `LocalHapticFeedback.current`, siempre de baja intensidad y sin saturar. Los tipos semánticos se **verifican con el compilador y contra el `minSdk` en la Fase 7a**; mientras tanto se usan los tipos de respaldo.

| Interacción | Tipo propuesto (NO VERIFICADO) | Respaldo existente |
| :--- | :--- | :--- |
| Cambio de pestaña en la barra flotante | `SegmentTick` | `TextHandleMove` |
| Confirmación de guardado en numpad / modal | `Confirm` | `LongPress` |
| Validación fallida o entrada rechazada | `Reject` | `LongPress` |
| Activar / desactivar chips o botones de selección | `ToggleOn` / `ToggleOff` | `TextHandleMove` |
| Conmutar partidas en Presupuesto / "Pagar mes" en Créditos | `Confirm` | `LongPress` |
| Acción destructiva confirmada (eliminar) | `Reject` | `LongPress` |

---

## 6. Navegación Flotante y FAB

La barra de navegación inferior opera como un overlay desacoplado, flotante y con fondo transparente sobre la capa Z superior:

- **Cápsula flotante:** contenedor tipo píldora (`CircleShape`, `surfaceContainerHigh`) con elevación tonal y sombra sutil. Su ancho se adapta dinámicamente con física de resorte.
- **Destino activo:** icono relleno (`selectedIcon`) + etiqueta en `labelLarge` con `FontWeight.Bold`, dentro de una píldora tonal (`secondaryContainer`). La etiqueta entra y sale con expansión horizontal y desvanecimiento.
- **Destinos inactivos:** únicamente el icono de contorno (`unselectedIcon`), centrado en un área táctil compacta, sin etiqueta.
- **FAB contextual:** botón cuadrado de esquinas redondeadas (`FABShape`, `56.dp × 56.dp`) a la derecha de la cápsula. Se reposiciona con resortes cuando cambia el ancho de la barra y se oculta con animación elástica en la pestaña de Ajustes. Todo el conjunto (cápsula + FAB) se mantiene centrado.
- **Desenfoque progresivo (API nativa de Compose 1.13, Android 13 o más):** el contenido pasa por debajo de zonas fijas y se desenfoca ahí con `Modifier.blur` y `BlurRadiusSpec.verticalGradient` (`components/BlurHeaderScaffold.kt`).
  - **Arriba:** las cuatro pestañas usan `BlurHeaderScaffold`: la barra superior y lo que deba quedar fijo (resumen, pestañas, selector de periodo) forman un encabezado que se mide primero; su alto entra al relleno de la lista, que lleva `underHeaderBlur`. Desenfoque máximo en el borde y nulo 12 dp después del encabezado, con un velo del color de fondo. Sin franja aparte: la barra es transparente.
  - **Abajo:** `bottomEdgeBlur` en el pager, fijo detrás de la cápsula flotante (140 dp). Solo en el estilo flotante.
  - **Aire:** `TopBarBreathing` (12 dp) sobre las barras superiores.
  - **Antes de Android 13** no hay desenfoque progresivo: arriba queda un velo casi opaco y abajo nada. Las subpantallas (Ajustes, Reportes…) conservan el velo de color de `fadingTopEdge`.
  - Exige `compileSdk` 37.1 (Compose UI 1.13).
- **Margen de seguridad:** toda lista con scroll aplica `contentPadding = PaddingValues(bottom = 120.dp)` para que ningún elemento quede tapado por la cápsula o el FAB.
- **Restricción para el dev:** no envolver con `TooltipBox` ni usar `weight` dentro del `RowScope` de la barra (rompe el layout).

---

## 7. Semántica Financiera y Color

Los colores contables se aíslan en `LocalFinancialColors` (`ExtendedFinancialColors`) a través de `MinicashThemeExtensions.financialColors`.

### Reglas semánticas

- **Ingreso (+):** verde esmeralda tonal equilibrado (`financialColors.income`).
- **Gasto (-):** rojo de error del tema (`financialColors.expense`).
- **Transferencia:** acento terciario (`financialColors.transfer`).
- **Cero / neutro:** sobre variante de superficie (`financialColors.neutral` / `onSurfaceVariant`).
- **PROHIBIDO:** hardcodear colores hexadecimales (por ejemplo `Color(0xFF2E7D32)`) o valores directos dentro de los componentes de UI.

### Regla inmutable de accesibilidad

> **El color NUNCA va solo.** Todo monto financiero o estado contable debe ir acompañado de su signo numérico explícito (`+` o `-`) o de un icono descriptivo (flecha arriba/abajo, insignia de tendencia).

### Montos

- Siempre `Long` en centavos. Prohibido el punto flotante para dinero.

### Tinte contextual y estados

- **Pantallas de detalle (Cuenta / Categoría):** el encabezado adopta un tinte derivado de su `colorHex` registrado. La derivación es una aproximación por mezcla de color (no una paleta tonal HCT completa); el texto sobre el encabezado teñido debe cumplir un contraste mínimo de 4.5:1.
- **Estados de presupuesto:** umbrales (normal, advertencia, agotado) expresados con cambio de color y **siempre** con texto explícito de porcentaje y estado.

---

## 8. Tipografía y Jerarquía Visual

### Fuente local offline

- **Quicksand** cargada estrictamente de forma local en `res/font/` (`quicksand_regular`, `quicksand_semibold`, `quicksand_bold`) y expuesta mediante `LocalRoundedFontFamily`.
- **Pesos disponibles:** `FontWeight.Normal`, `FontWeight.SemiBold` y `FontWeight.Bold`. No se usan otros pesos (por ejemplo `ExtraBold`), porque no existen en los archivos locales.

### Roles tipográficos

Los valores son el punto de partida y se afinan tras revisión visual en la Fase 3.

| Rol | Estilo | Peso | Color |
| :--- | :--- | :--- | :--- |
| **Título de pantalla** | `headlineLarge` | `Bold` | `onSurface` |
| **Título de sección** (fuera de tarjetas) | `titleMedium` | `Bold` | `onSurface` |
| **Cifra protagonista** (una sola por tarjeta o bloque) | `headlineMedium` | `Bold` | semántico (ver sección 7) |
| **Título de fila** | `bodyLarge` | `SemiBold` | `onSurface` |
| **Monto de fila** | `titleMedium` | `SemiBold` | semántico (ver sección 7) |
| **Texto secundario** | `bodySmall` | `Normal` | `onSurfaceVariant` |
| **Etiqueta pequeña / badge** | `labelSmall` | `SemiBold` | `onSurfaceVariant` |

### Cifras tabulares e idioma

- Toda cifra monetaria, porcentaje o saldo contable incluye obligatoriamente:

```kotlin
fontFeatureSettings = "tnum"
```

- **Sentence case obligatorio en español:** todos los títulos, etiquetas, botones y mensajes usan estilo de oración ("Guardar movimiento", "Gastos fijos", "Categorías registradas"). Prohibido el Title Case y el texto sin traducir en la UI.

---

## 9. Implementación y Acceso a Tokens en Código

Ejemplos canónicos de acceso a tokens en composables de Minicash:

```kotlin
// Acceso a colores financieros centralizados
val financialColors = MinicashThemeExtensions.financialColors
val incomeColor = financialColors.income

// Acceso a espaciados normalizados
val spacing = MinicashThemeExtensions.spacing
Modifier.padding(horizontal = spacing.standard, vertical = spacing.small)

// Uso de tokens de forma propios
Surface(
    shape = MinicashShapes.ContainerLarge,
    color = MaterialTheme.colorScheme.surfaceContainerLow
) { /* Contenido */ }

// Aplicación de listas segmentadas
val itemShape = rememberSegmentedShape(index = index, count = totalCount)
Card(shape = itemShape) { /* Fila segmentada */ }
```

---

## 10. Matriz de Adopción de Componentes M3E

| Componente | Tipo de Implementación | Pantalla / Módulo | Estado |
| :--- | :--- | :--- | :--- |
| Barra flotante con FAB | Propia desacoplada con resortes | `MainScreen` | Implementado |
| Listas segmentadas | Helper propio `rememberSegmentedShape` | Historial, Fijos, Créditos, Cuentas | Implementado (estático) |
| Wavy Progress Indicator | `WavyProgressIndicator` propio (Canvas) | Créditos, Fijos | Implementado (provisional) |
| Hero de 3 tarjetas | Propio (Patrimonio, Ingresos, Gastos) | `HomeScreen` | Implementado (simétrico; ver sección 11) |
| Donut Chart Split | Canvas propio horizontal con Top 4 | `HomeScreen` | Implementado |
| Numpad con calculadora | Propio embebido con cinta de operadores | `AddTransactionSheet` | Implementado |
| Chips en píldora | `FilterChip` (`CircleShape`) | Historial, Ajustes | Implementado |
| Animación en listas segmentadas | Morphing de esquinas con `motionScheme` | Historial, Fijos, Créditos | Pendiente |
| `ButtonGroup` / `ToggleButton` | M3E oficial (compila con la estable 1.5.0) | Selectores de tipo y filtros | Pendiente |
| `LoadingIndicator` | M3E oficial (verificado que compila) | Carga de reportes y restauración | Pendiente |
| `LinearWavyProgressIndicator` | M3E oficial (verificado que compila) | Créditos, Fijos | Pendiente (evaluación comparativa) |

---

## 11. Decisiones Pendientes del PO

Estas decisiones no se cambian sin aprobación explicita del PO:

1. **Hero de Inicio:** hoy es simétrico de 3 tarjetas. Queda abierta la opción de evolucionarlo a un diseño asimétrico (una pieza grande de patrimonio y dos piezas pequeñas apiladas).
2. **Radio del FAB:** `FABShape` está en 18.dp. Puede ajustarse a un radio mayor tras revisión visual contra la referencia.
