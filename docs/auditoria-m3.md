# Auditoría de diseño M3 Expressive: Minicash

Fecha: 2026-11-11. Base: skill `m3-expressive` (`.claude/skills/m3-expressive`, solo texto). Cada valor de M3 cita su archivo de la skill.
Lo que M3 no define se marca como **decisión de diseño**. No se cambió código por esta auditoría.

## Resumen
- **Cumple:** transición de pestañas en fundido ("top level"), eje X compartido con fundido para subpantallas ("forward and backward"), atrás predictivo, barra integrada en `surface container` sin sombra, volver a tocar la pestaña sube al tope, la barra se oculta al desplazar, diálogos de 28 dp y hojas con 28 dp arriba.
- **Por atender:** 3 prioridad media, 5 baja y 3 decisiones de diseño que conviene confirmar.

## Estado de las correcciones (2026-11-14)
| # | Hallazgo | Estado |
|---|---|---|
| 1 | Barra oculta con lector de pantalla | Corregido: con exploración táctil activa la barra flotante se queda visible. |
| 2 | Íconos inactivos con alfa 0.75 | Corregido: usan `onSurfaceVariant` completo. |
| 3 | Radios 24 y 18 dp | Corregido: 24 → 20 dp (`MinicashShapes.ContainerLarge` y literales), 18 → 16 dp. |
| 4 | FAB de 52 dp / 18 dp | Corregido: 56 dp con esquina de 16 dp; pulsado se redondea a 28 dp. |
| 5 | Barra superior grande | Corregido: `LargeFlexibleTopAppBar`. |
| 6 | Rebote del ícono | Corregido: `fastSpatialSpec` del esquema de movimiento. |
| 7 | Duraciones fijas en transiciones | Parcial: el fundido de pestañas usa resortes de efectos; el eje X compartido conserva `tween` (permitido para transiciones y sin rebote). |
| 8 | Entrada de diálogos con escala | Corregido: fundido corto y expansión vertical; sin elevación tonal extra. |

## Revisión general de UX (2026-11-14 y 15)
Segunda ronda, hecha en cinco fases con prueba en el teléfono. Detalle completo de cada hallazgo en la propuesta original; aquí solo su estado.

| # | Hallazgo | Estado |
|---|---|---|
| 1 | Espacio duplicado sobre los títulos | Corregido (fase A): las barras superiores dentro del panel no aplican de nuevo el margen del sistema. |
| 2 | Zonas táctiles menores a 48 dp | Corregido (fase B): flechas de mes, palomita, engrane y atrás miden 48 dp con el mismo tamaño visual. |
| 3 | Título duplicado en Inicio | Corregido (fase A): un solo título de sección y "Gastos" / "Ingresos" dentro de la tarjeta. |
| 4 | El botón de agregar tapa contenido | Corregido (fase A): relleno inferior revisado en cada lista. |
| 5 | Nombres distintos para lo mismo | Corregido (fase A): "Movimientos", "Quincenas" (antes «Gastos fijos») y "Créditos" en barra, títulos y Extras. |
| 6 | Jerarquía tipográfica plana | Corregido (fase D): pesos y tamaños en escala. |
| 7 | Campos de texto con cuatro formas | Corregido (fase C): una sola forma. |
| 8 | Hojas sin protección al descartar | Corregido (fase C): las que capturan datos usan la versión protegida. |
| 9 | Selector de fecha antiguo | Descartado: era solo un import sin uso. |
| 10 | Indicadores de carga | Corregido (fase C): `LoadingIndicator`. |
| 11 | Eliminar sin deshacer | Corregido (fase E): se borra al instante y un aviso ofrece "Deshacer" (movimientos y partidas). |
| 12 | Listas de Ajustes sin estilo expresivo | Corregido (fase D): listas segmentadas. |
| 13 | Háptica inconsistente | Corregido (fase C): un solo ayudante. |
| 16 | Menú del botón de agregar | Hecho: despliega Gasto, Ingreso y Transferencia. |
| 17 | Tamaños de texto fijos en gráficas | Corregido (fase D). |
| 18 | Textos fuera de `stringResource` | **Pendiente a propósito**: la app no se puede traducir. |
| 19 | Archivos muy grandes | **Pendiente a propósito**: es mantenibilidad, no pantalla. |

Cambios posteriores sobre la barra flotante: el botón de agregar es un círculo del mismo alto que la cápsula; la cápsula ahora tiene ancho fijo (el del destino más largo) y solo reparte el espacio entre las píldoras, de modo que no se redimensiona, no mueve el botón y las formas no se recortan en seco al animarse. Se quitó la tarjeta con el logo al final de Ajustes porque repetía "Acerca de".

Revisión de color y tipografía: sin uso de roles `container` como color de texto o ícono, y los divisores usan `outline variant` por defecto. Los pesos Bold de titulares y acciones ya equivalen a los estilos enfatizados, así que no se cambió la escala tipográfica.

## Prioridad media

### 1. La barra no debe ocultarse al desplazar con lector de pantalla activo
- **Hoy:** `MainScreen.kt` oculta la barra flotante al bajar sin importar la accesibilidad.
- **M3:** "Don't hide the nav bar on scroll when a screen reader is active" (`components/navigation.md`, Navigation bar › Scrolling y Accessibility).
- **Propuesta:** consultar `AccessibilityManager.isTouchExplorationEnabled` y mantenerla visible.

### 2. Íconos inactivos de la barra flotante con alfa 0.75
- **Hoy:** `onSurfaceVariant.copy(alpha = 0.75f)`.
- **M3:** íconos activo e inactivo con contraste mínimo de 3:1 contra el contenedor (`navigation.md`, Accessibility).
- **Propuesta:** usar `onSurfaceVariant` sin alfa y medir el contraste en claro, oscuro y AMOLED.

### 3. Esquinas fuera de la escala de 10 pasos
- **Hoy:** 24 dp (9 usos, `MinicashShapes.ContainerLarge`) y 18 dp (6 usos, incluido `FABShape`).
- **M3:** escala 0 / 4 / 8 / 12 / 16 / **20** / 28 / **32** / **48** / full (`tokens.md`, Corner radius scale). 24 y 18 no existen.
- **Propuesta:** 24 → 20 (tarjetas de contenido) o 28 (contenedores hero); 18 → 16. Respetar `interior = exterior − relleno` en anidados.

## Prioridad baja

### 4. FAB de 52 dp con esquina de 18 dp
- **M3:** FAB base 56 dp con forma `CornerLarge` (16 dp); `component-tokens.md` › FabBaseline. En móvil recomienda el FAB mediano de 80 dp (`components/fabs.md`).
- **Propuesta:** 56 dp / 16 dp. Pulsado: el patrón de morfeo de botones.

### 5. Barra superior grande: usar la versión flexible
- **Hoy:** `LargeTopAppBar` en `MinicashSubScreenTopBar`.
- **M3:** las barras "medium/large flexible" reemplazan a las "medium/large"; estas dejan de recomendarse (`components/bars.md`, M3 Expressive update).
- **Propuesta:** `LargeFlexibleTopAppBar`, disponible en Material3 1.5.0-beta01 (verificado en el jar de fuentes).

### 6. Rebote del ícono en la barra flotante
- **Hoy:** `spring(dampingRatio = 0.45f, stiffness = 380f)`.
- **M3:** resortes espaciales expresivos: default 0.8/380, fast 0.6/800, slow 0.8/200 (`tokens.md`). 0.45 rebota más que cualquier token.
- **Propuesta:** `MaterialTheme.motionScheme.defaultSpatialSpec()` (o `fast`, que es un elemento pequeño).

### 7. Duraciones fijas en las transiciones de pantalla
- **Hoy:** `tween` de 90 / 210 / 300 ms (fundido de pestañas y eje X compartido).
- **M3:** transiciones aún pueden usar easing y duración "legacy" (`transitions.md`), pero para trabajo nuevo recomienda resortes (`motion.md`). Los efectos de opacidad deben ir con resortes de efectos (sin rebote).
- **Propuesta:** valorar resortes de efectos para los fundidos. Es opcional; no se nota funcionalmente.

### 8. Entrada de diálogos con escala
- **Hoy:** `MinicashAlertDialog` entra con fundido y escala.
- **M3:** en Android, los componentes dentro de la pantalla se expanden en x o y; "evitar escala y movimiento en z" porque implica elevación; los fundidos deben ser cortos (`transitions.md`, Enter and exit).
- **Propuesta:** fundido corto con expansión vertical ligera, o dejar la escala como decisión de diseño.

## Decisiones de diseño a confirmar (desvían de M3 a propósito)

### A. Etiquetas solo en el destino activo (barra flotante)
- **M3:** etiqueta obligatoria en todos los destinos; "Don't remove the labels" (`navigation.md`).
- **Contexto:** la cápsula flotante se parece más a una *floating toolbar*; la integrada sí muestra todas las etiquetas.
- **Recomendación:** mantener, pero el `contentDescription` ya existe en cada ícono, y la barra integrada cubre quien necesite etiquetas.

### B. Deslizar entre pestañas
- **M3:** "Don't swipe between destinations"; la transición lateral no es para destinos de nivel superior (`navigation.md`, `transitions.md` › Do/Don't).
- **Hoy:** el pager desliza y además escala 0.95 y baja la opacidad 35 %.
- **Elegido por el usuario:** fundido al tocar y deslizar al arrastrar.
- **Si se quiere conformidad total:** quitar el deslizamiento del pager (`userScrollEnabled = false`).

### C. Barra superior siempre del color del fondo, con velo al desplazar
- **M3:** la barra parte del color del fondo y al desplazar cambia a `surface container`, sin sombra (`bars.md`).
- **Hoy:** siempre color de fondo, más un velo en degradado inspirado en Pixel Budget Tracker.
- **Recomendación:** mantener; es una decisión de estilo coherente en todas las pantallas.

## Otros puntos revisados sin hallazgo
- Hojas inferiores: 28 dp arriba y asa visible. Difieren en el color del contenedor (`surfaceContainerLowest` contra `surface container low` de M3) y en que algunas arrancan a pantalla casi completa; como no tienen alturas preestablecidas no requieren alternativa de un solo puntero.
- Diálogos: color `surface container high`, 28 dp, botones al borde final; cumple.
- Altura de botones: los de Minicash miden 48 dp o más; M3 usa 40 dp (S) y 56 dp (M). Son tamaños cómodos para pulgar; **decisión de diseño** aceptable.
- Progreso ondulado: M3 tiene pista de 4 dp por defecto y forma ondulada disponible; el de Minicash usa 3.5 dp y es propio. Bajo impacto.

## Orden sugerido para corregir
1. Puntos 1 y 2 (accesibilidad), de bajo riesgo.
2. Punto 3 (radios), que toca muchas pantallas y conviene revisar en el teléfono.
3. Puntos 4, 5 y 6, de ajuste local.
4. Puntos 7 y 8, y las decisiones A–C, según el gusto del usuario.
