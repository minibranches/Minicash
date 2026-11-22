# Guía de Diseño y Sistema de Tokens - Minicash (Material 3 Expressive)

Este documento establece las especificaciones inmutables de UI, movimiento y tokens para **Minicash**, gobernado por **Material 3 Expressive** y Jetpack Compose.

> **Convención:** todo lo marcado como **NO VERIFICADO** es una propuesta que aún no ha compilado en este proyecto. Se confirma con el compilador en la fase indicada antes de usarse; hasta entonces no debe darse por existente.

---

## 1. Jerarquía de Contenedores Tonales y Superficies

Minicash estructura la profundidad visual mediante los niveles tonales de superficie de Material 3:

| Nivel de Superficie | Rol en Minicash | Token de Forma Asociado |
| :--- | :--- | :--- |
| **`surfaceContainer`** (claro) / **`surface`** (oscuro) | Fondo de pantalla y Scaffolds (`screenBackground`): plano, de borde a borde, sin marco ni esquinas redondeadas arriba | Plano (`0.dp`) |
| **`surfaceBright`** (claro) / **`surfaceContainer`** (oscuro) | Tarjetas, filas y hojas inferiores (`cardSurface`, `sheetBackground`): siempre más claras que el fondo | `MinicashShapes.Card` (28.dp), `SheetTop` (32.dp) |
| **`surfaceContainerLow`** | Tarjeta de segundo nivel (`CardTonalLevel.DEFAULT`) | `MinicashShapes.Card` (28.dp) |
| **`surfaceContainerHigh`** | Pistas de pestañas, selectores y chips (`trackSurface`), cápsula flotante, botones circulares y diálogos | `Pill`, `MinicashShapes.Hero` (32.dp) |
| **`surfaceContainerHighest`** | Avance, destacados y micro-badges | `Pill` |

> **Fondo y tarjetas:** la jerarquía se invirtió respecto a la 1.1: el fondo es el gris tonal y las tarjetas son las claras (como en las referencias de Pixel). En **claro** el fondo es `surfaceContainer` y las tarjetas `surfaceBright`; en **oscuro** el fondo es `surface` (mucho más oscuro) y las tarjetas `surfaceContainer`, de modo que las pistas, chips y elementos elevados (`surfaceContainerHigh` y `Highest`) siguen viéndose sobre ellas. `surfaceContainerLowest` ya no se usa como fondo. En AMOLED el fondo es negro y las tarjetas `#0F0F0F`. Los tintes del esquema dinámico oscuro son bajos (0.04 a 0.10) para no aclarar el fondo. Se accede con `MaterialTheme.colorScheme.screenBackground`, `.cardSurface`, `.sheetBackground`, `.trackSurface` y `.dialogSurface` (`ui/theme/Theme.kt`).

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
| **`Card`** | `RoundedCornerShape(28.dp)` | Tarjetas interactivas estándar de listas y transacciones |
| **`ContainerLarge`** | `RoundedCornerShape(28.dp)` | Agrupadores principales, tarjetas de resumen y Donut |
| **`Hero`** | `RoundedCornerShape(32.dp)` | Contenedores principales de cabecera, diálogos |
| **`SheetTop`** | `RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)` | Hojas inferiores |
| **`Pill`** | `CircleShape` | Botones de acción, chips de filtro y pestañas activas |
| **`FABShape`** | `RoundedCornerShape(18.dp)` | Botón de acción flotante (FAB) cuadrado suavizado |

### Contraste de formas y listas segmentadas

- **Contraste de formas:** el elemento protagonista de una pantalla se distingue tanto por escala como por una curvatura más pronunciada respecto a las tarjetas secundarias.
- **Listas segmentadas:** todo grupo continuo (movimientos por día, partidas presupuestales, cuentas, créditos) utiliza el helper `rememberSegmentedShape`:
  - **Elemento único (`count == 1`):** 4 esquinas con `ContainerLarge` (28.dp).
  - **Primer elemento (`index == 0`):** esquinas superiores `ContainerLarge` (28.dp), inferiores `Sharp` (4.dp).
  - **Elementos intermedios:** las 4 esquinas `Sharp` (4.dp).
  - **Último elemento (`index == count - 1`):** esquinas superiores `Sharp` (4.dp), inferiores `ContainerLarge` (28.dp).
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
| **`screen`** | `20.dp` | Margen lateral de las pantallas |
| **`extraLarge`** | `24.dp` | Separación entre bloques principales |
| **`hero`** | `32.dp` | Separación entre secciones mayores de una pantalla |

### Ritmo y espacio en pantalla

- Margen lateral: `spacing.screen` (20.dp).
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
- **Prohibición:** no se usan curvas `tween` ni interpoladores lineales arbitrarios salvo justificación técnica documentada. Las únicas excepciones son las animaciones infinitas (onda del progreso y rotación del onboarding).
- **Un solo juego de entradas y salidas (`theme/MotionSpecs.kt`):** todo `AnimatedVisibility` o `AnimatedContent` nuevo usa `MinicashTransitions` (`enterVisible`/`exitVisible`, `enterFloat`/`exitFloat`, `contentSwap`) y toda lista usa `minicashAnimateItem()` en lugar de `animateItem()`. Salen del `motionScheme` (espacial para tamaño y desplazamiento, efectos para opacidad) y respetan `LocalAnimationRestriction`. Los conteos usan `minicashSlowEffectsSpec` (amortiguamiento 1.0: no rebasan el valor final).
- **Encabezado plegable (`BlurHeaderScaffold`):** la barra con el título se esconde al bajar por la lista y reaparece de inmediato al subir (comportamiento *enterAlways*); al soltar se acomoda con resorte al extremo más cercano. La fila fija (`pinned`: periodo, búsqueda y filtros, resumen) sube a ocupar su lugar. El alto completo sigue entrando al relleno de la lista, así que no hay saltos. El velo de `underHeaderBlur` sigue al alto visible (`LocalHeaderVisiblePx`).
- **Estadios en una fila:** Balance, Ingresos y Gastos de Movimientos van en una sola fila con `StatPillMoney(compact = true)` (menos relleno, `titleMedium` y `autoSizeMin` de 11 sp en `MoneyTextView`). Los centavos usan tamaño relativo (`0.62.em`) para escalar junto con la cifra.
- **Modo de rendimiento (Apariencia):** límite de frecuencia de la app (Automático, 30, 60, 120 Hz) guardado en `UserPreferences.refreshRateLimit` y aplicado a la ventana con `preferredRefreshRate` (`ui/utils/RefreshRate.kt`). 120 Hz solo se ofrece si la pantalla llega a ~120 Hz. Verificado en un Nothing Phone: 30/60/120 Hz producen 490/978/1902 cuadros en la misma prueba. No entra en las copias de seguridad (depende de la pantalla del dispositivo).
- **Campo que crece sin desbordar:** un contenedor que recorta a un hijo más ancho (p. ej. `animateContentSize` sobre un campo de texto) hace que Compose intente traer el campo a la vista y desplace el `HorizontalPager`. Los campos que crecen se miden al ancho animado (`layout {}`), nunca recortados.
- **Búsqueda desde el ícono y filtros en píldora:** en Movimientos la lupa se expande con resorte hacia la izquierda y se vuelve el campo de búsqueda (← cierra y limpia). `ExpandingFilterButton` es un ícono de 48 dp que, con filtros aplicados, crece a una píldora «N filtros ×» (Movimientos y Créditos).

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
- **Zonas fijas sobre la lista (velo de color):** el contenido pasa por debajo del encabezado y de la cápsula flotante y se atenúa con un velo del color de fondo (`underHeaderBlur` en `components/BlurHeaderScaffold.kt`).
  - **Arriba:** las cuatro pestañas usan `BlurHeaderScaffold`: la barra superior y la fila fija forman un encabezado que se mide primero; su alto entra al relleno de la lista. Velo casi opaco dentro del encabezado que se desvanece 12 dp después. Sin franja aparte: la barra es transparente.
  - **Abajo:** velo que sube desde el borde inferior de la lista (140 dp con la cápsula flotante, 64 dp en subpantallas).
  - **Aire:** `TopBarBreathing` sobre las barras superiores.
  - **Desenfoque progresivo nativo apagado (1.3):** el código de `Modifier.blur` con `BlurRadiusSpec` (Compose 1.13, Android 13+) sigue en el archivo tras `PROGRESSIVE_BLUR_ENABLED = false`. Medido en un Nothing Phone a 120 Hz con el build `perf` (Movimientos, desplazamiento continuo, mediana de dos pasadas): con desenfoque, 16 ms por cuadro (≈ 60 fps), con radio de 18 dp y con 10 dp por igual; sin desenfoque, 9 ms. El costo es desenfocar una capa a pantalla completa, no el radio. Con el velo y el encabezado plegable queda en 11-12 ms. Se puede volver a probar en hardware más rápido.
  - Compilar con `compileSdk` 37.1 sigue siendo necesario para esa API.
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
| Estadio de dato (`StatPill`, `StatPillMoney`) | Propio: cifra grande, etiqueta y avance como relleno del estadio | Movimientos, Créditos, Gastos fijos, Reportes | Implementado |
| Aviso punteado (`InsightCard`) | Propio: borde punteado y destello, solo para mensajes que la app ya muestra | Reportes (mensaje de ahorro) | Implementado |
| Sección plegable (`CollapsibleSectionHeader`) | Propio: píldora con contador y flecha | Créditos (Liquidados) | Implementado |
| Chip de filtro tonal (`MinicashFilterChip`) | `FilterChip` en píldora, sin borde | Movimientos y hojas de filtros, Ajustes, Onboarding | Implementado |
| Ícono festoneado (`ScallopedIcon`) | `MaterialShapes.Cookie9Sided` | Filas de opciones, estados vacíos, avatares de movimientos | Implementado |
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

## 10b. Modales de alta y edición

- **Un solo cuerpo para alta y edición de movimientos:** encabezado con título y fecha, selector de tipo siempre visible (Gasto, Ingreso, Transferencia; ya no hay un paso aparte para elegirlo), monto grande con números tabulares, concepto, selectores de categoría y cuenta, teclado numérico y botón fijo abajo.
- **Teclado:** el concepto ya no toma el foco al abrir (el teclado del sistema no tapa el numérico ni el botón). Alta y edición usan las mismas reglas del teclado (`processKeypadPress`: máximo 2 decimales, sin ceros a la izquierda). Las teclas son botones accesibles (rol, descripción y acción de clic).
- **Acciones:** alta usa «Guardar» y edición «Guardar cambios»; si no se puede guardar, un texto dice qué falta. Eliminar tiene un único botón, abajo, y siempre pide confirmación («¿Eliminar movimiento?»); después el aviso ofrece «Deshacer».
- **Descartar cambios:** protege concepto, monto, nota, categoría, cuentas y fecha.
- **Compra a meses:** al editar, «Guardar» está disponible desde cualquier paso.
- **Etiquetas:** sentence case, sin dos puntos al final, «Transferencia» sin abreviar y plural correcto («Copiar 1 partida»).

## 11. Decisiones del PO

1. **Hero de Inicio:** hoy es simétrico de 3 tarjetas. Queda abierta la opción de evolucionarlo a un diseño asimétrico (una pieza grande de patrimonio y dos piezas pequeñas apiladas).
2. **Lenguaje visual (1.2):** se toma como referencia el estilo de las apps Pixel (fondo gris, tarjetas claras, radios de 28 a 32 dp, estadios para las cifras, íconos festoneados, chips tonales). No se incorpora nada de Liquid Glass ni efectos similares: la cápsula flotante es sólida y los diálogos solo atenúan el fondo. El desenfoque progresivo nativo se probó y se apagó en la 1.3 por rendimiento (sección 6); queda el velo de color.
3. **FAB:** `FABShape` queda en 18.dp (cuadrado suavizado).
