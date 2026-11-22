# Cambios

Las versiones siguen [SemVer](https://semver.org/lang/es/). Cada versión se publica como *release* en GitHub con su tag `vX.Y.Z`.

## 1.2.0

### Novedades
- **Nuevo lenguaje visual.** Fondo gris con tarjetas más claras (en oscuro, un fondo mucho más oscuro con las tarjetas un nivel arriba), sin el marco oscuro, con radios de 28 a 32 dp, margen lateral de 20 dp, íconos festoneados y centavos más pequeños en los montos.
- **Estadios para las cifras.** Movimientos, Créditos, Gastos fijos y Reportes muestran sus estadísticos en estadios, con el avance como relleno. Son los mismos datos de siempre.
- **Créditos en una sola lista.** Los activos arriba y los liquidados plegados al final en «Liquidados · N».
- **Movimientos.** Filtros activos como chips tonales y fecha de hoy bajo el saludo de Inicio.
- **Modales de movimientos.** El tipo se elige dentro del formulario, el teclado ya no choca con el del sistema, «Guardar» dice qué falta, y eliminar pide confirmación. Editar una compra a meses permite guardar desde cualquier paso.
- **Onboarding.** El campo del nombre ya no queda tapado por el teclado.

### Ajustes tras la primera revisión
- **Movimientos:** el balance va primero; Ingresos y Gastos son filtros rápidos (tocar uno filtra por tipo y tocarlo de nuevo lo quita). Los chips de Tipo, Cuenta y Categoría salen de la pantalla: esos filtros viven solo en la hoja de filtros.
- **Desenfoque inferior:** ahora se aplica a las listas y no a toda la página, así el aviso «Deshacer» y la barra de selección múltiple quedan por encima, sin desenfocar.
- **Gastos fijos:** el botón de copiar tiene el mismo estilo que Reportes y Ajustes, y el check de las partidas pagadas usa un sello festoneado de 12 lóbulos.
- **Créditos:** el filtro está en la misma fila que la deuda pendiente y el pago mensual; se quitó la etiqueta «Activos».
- **Ajustes:** los íconos de las tres secciones tienen el mismo color.
- **Selector de hora:** dos ruedas (hora y minutos) con AM/PM, como en las referencias, en los cuatro lugares donde se elige una hora.
- **AMOLED:** tarjetas, botones y fondos de lista menos oscuros.

### Correcciones
- **Fecha del selector de noche.** Al abrir el calendario de Nuevo o Editar movimiento por la noche (zonas atrasadas respecto a UTC, como México) se mostraba el día siguiente, y aceptar sin cambiar nada guardaba esa fecha. Ahora el selector parte del día local.

### Interno
- Componentes nuevos: `StatPill`, `InsightCard`, `CollapsibleSectionHeader`, `MinicashFilterChip`, `ScallopedIcon`.
- Debug con sufijo `.debug`: se instala junto a la app real sin tocar sus datos.

No cambia el formato de datos: no hace falta migrar ni restaurar nada. Aun así, conviene tener una copia de seguridad antes de actualizar.

## 1.1.0

### Novedades
- **Navegación nueva.** La barra tiene Inicio, Movimientos, Gastos fijos y Créditos (los módulos que ocultes no aparecen). Desaparece la pestaña Extras: Reportes y Ajustes están arriba a la derecha de cada pestaña. El botón de agregar está en las cuatro pestañas; en Inicio y Movimientos despliega Gasto, Ingreso y Transferencia. Con la barra flotante, las píldoras cerradas se encogen en pantallas angostas para no desbordar.
- **Desenfoque progresivo (Android 13 o más).** El contenido que pasa bajo la zona del título y detrás de la barra flotante se desenfoca de forma fija, con la API nativa de Compose (`Modifier.blur` con `BlurRadiusSpec`). Las subpantallas lo tienen arriba y abajo. Antes de Android 13 queda un velo del color de fondo.
- **Más aire arriba.** El saludo y los botones de las barras superiores tienen más espacio, y la barra superior es transparente en lugar de una franja de color.
- **Material 3 estable.** Se actualiza a `material3 1.5.0` y Compose UI 1.13 (requiere la plataforma Android 37.1 para compilar).

### Correcciones
- **Widgets en la versión release.** Los dos widgets se quedaban cargando: R8 le quitaba el constructor a una clase de WorkManager que Glance usa para cargarlos. Se agregan las reglas de conservación necesarias y los errores de actualización ahora se registran en logcat.
- **Parpadeo al abrir.** Al abrir la app desde cero se veía un instante con el tema predeterminado antes de pasar al color configurado. Ahora el tema se lee antes de dibujar y el primer cuadro ya sale con tu color y tu estilo de barra. El *splash* también respeta el modo oscuro.
- **Lista de Créditos invisible.** Al entrar a Créditos desde Gastos fijos las tarjetas podían no dibujarse hasta tocar la lista. Ahora la lista se vuelve a medir al activarse la pestaña.

### Interno
- Pruebas nuevas para el tema precargado y los destinos de la barra.
- Regla 5 de `CONTRACTS.md` actualizada: se permite el desenfoque progresivo nativo detrás de la cápsula; siguen prohibidos los fondos opacos, `Modifier.weight` y `TooltipBox` en la barra.

No cambia el formato de datos: no hace falta migrar ni restaurar nada. Aun así, conviene tener una copia de seguridad antes de actualizar.

## 1.0.0

Primera versión publicada: inicio con patrimonio y distribución, movimientos con transferencias, detalle de cuenta, gastos fijos por quincenas, créditos, reportes, copias de seguridad (JSON y CSV), widgets, bloqueo de la app y personalización de tema.
