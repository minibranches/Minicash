# Cambios

Las versiones siguen [SemVer](https://semver.org/lang/es/). Cada versión se publica como *release* en GitHub con su tag `vX.Y.Z`.

## 1.5.1

Versión de corrección.

### Correcciones
- **Transferencias.** Al crear una transferencia con las cuentas que propone la app (sin abrir el selector de «A:»), «Guardar» parecía activo pero no hacía nada. Ahora la hoja y el guardado usan la misma regla para elegir origen y destino. Al editar un movimiento para convertirlo en transferencia ya no se guarda sin cuenta destino.

### Interno
- `resolveTransferAccounts`, con pruebas. No cambia el formato de datos.

## 1.5.0

### Novedades
- **Créditos en tarjetas.** La lista pasa a una cuadrícula de dos columnas, como Gastos fijos: ícono con el color de la institución, porcentaje, barra de progreso ondulada expresiva, «n de total», monto y botón «Pagar mes». Los liquidados quedan en su sección plegable.
- **Patrimonio en una franja** de una línea (cifra a la derecha) en lugar de la tarjeta alta; la tarjeta sigue disponible como opción.
- **Cuentas de Inicio con acabado por tipo.** Todas del mismo tamaño y con el color de cada cuenta: débito con chip, crédito con borde y etiqueta «A pagar», efectivo con borde punteado y «$» de fondo, ahorro con monedas. El catálogo de cuentas de Ajustes no cambia.
- **«Distribución del mes» con cuatro estilos:** dona, barras, barra apilada (nueva predeterminada) y Top 5 con la variación contra el mes anterior.
- **Ajustes → Inicio.** Muestra u oculta y reordena (arrastrando) Patrimonio, Mis cuentas, Distribución del mes y Movimientos recientes; elige el estilo de la distribución y del patrimonio.
- **Ajustes reordenados.** Cuatro grupos con etiqueta pequeña (Tu dinero, Preferencias, Privacidad y datos, Acerca de) y filas de una línea con su valor actual. Las filas llevan solo la flecha a la derecha. Nueva subpantalla **Notificaciones** con el aviso rojo, la hora de los avisos de pago y el recordatorio diario.

### Interno
- Preferencias de Inicio en DataStore (no entran en la copia de seguridad). `HomeLayout`, `DistributionMath` y las rutas nuevas, con pruebas.
- No cambia el formato de datos ni la base de datos.

## 1.4.0

### Novedades
- **Deslizar para eliminar.** En Movimientos cada fila se elimina deslizándola de derecha a izquierda: aparece un fondo rojo con el bote de basura, que da un pequeño rebote al pasar el umbral. Se borra con el aviso «Deshacer» de siempre. Se quitó el botón de basura de cada fila. Con la selección múltiple activa no se desliza.
- **Sin gesto entre pestañas.** Las pestañas se cambian solo desde la barra inferior, para que el deslizamiento horizontal quede libre para eliminar.
- **Filtros rápidos en Gastos fijos.** Ingresos, Reembolsos y Gastos del resumen filtran las partidas con un toque (otro toque quita el filtro). Con un filtro activo no se reordena por arrastre.
- **Tarjetas de Gastos fijos más bajas**, para ver más partidas sin desplazar.
- **Selector de mes de «Distribución del mes»** igual al de Movimientos (año, flechas y mes). No se puede ir a meses futuros.
- **Rebote en toda la app.** Tarjetas, píldoras, chips, filas de opciones, selector de periodo, barra de pestañas y menú del botón de agregar se encogen un poco al tocarlos y vuelven con un rebote; las entradas de teclado numérico, avisos y diálogos «saltan» al aparecer.

### Correcciones
- Los grupos de movimientos ya no muestran filas repetidas o equivocadas tras eliminar y deshacer, ni al restaurar una copia: cada fila lleva su propio estado ligado al movimiento.

### Interno
- `bounceOnPress`, `SwipeToDeleteRow`, `filterBudgetItems` y la lógica pura del selector de mes de Inicio, con pruebas.
- No cambia el formato de datos.

## 1.3.1

Versión de correcciones.

### Correcciones
- **Búsqueda de movimientos.** Ya se llega al último resultado, con o sin teclado abierto: las listas de las cuatro pestañas dejan el espacio justo para la barra flotante y el teclado, y el velo inferior solo se dibuja cuando hay algo más abajo. Además, el título ya no se esconde ni reaparece si la lista no se está moviendo (pocos resultados).
- **Teclados del movimiento nuevo y de la edición.** El teclado del sistema y el numérico ocupan el mismo lugar: se ve uno u otro según el teclado del sistema esté abierto, sin importar cómo se cierre (✓, atrás, tocar fuera, elegir una sugerencia, categoría o cuenta). «Guardar» (y «Guardar cambios») queda siempre fijo al pie, con el motivo cuando está deshabilitado, y ya no desaparece ni mueve la hoja. Los dos modales comparten el mismo teclado numérico.
- **Notificaciones.** Si hay un recordatorio diario o avisos de pago activos (por ejemplo, tras restaurar una copia), la app pide el permiso de notificaciones una vez al abrirse, y los interruptores reflejan el estado real del sistema. Si está desactivado, Ajustes muestra un aviso rojo con acceso directo a la configuración del sistema. El recordatorio diario ya no intenta notificar sin permiso.
- **Gastos fijos y Movimientos.** El velo del encabezado ya no roza la cifra «Disponible» ni el resumen sin haber bajado la lista.
- **Copiar quincena.** Las partidas siguen la regla de listas segmentadas: el primero y el último con esquinas redondeadas hacia afuera y los intermedios con un redondeo leve.
- **Ajustes.** Los subtítulos con un solo elemento van en singular («1 institución registrada»).

### Interno
- `tabListBottomPadding`, `NotificationNeeds` y `RequestNotificationPermissionOnce`.
- No cambia el formato de datos.

## 1.3.0

### Novedades
- **Encabezado que se pliega.** En Inicio, Movimientos, Gastos fijos y Créditos la barra con el título se esconde al bajar por la lista y reaparece al subir; lo que debe quedar a la vista (periodo, búsqueda y filtros, resumen) se queda fijo arriba.
- **Búsqueda que sale del ícono.** En Movimientos la lupa se expande hacia la izquierda y se vuelve el campo de búsqueda, con el teclado listo. La flecha cierra y limpia la búsqueda. Arriba, a la izquierda, se muestra el periodo que estás viendo.
- **Filtros en píldora.** El botón de filtros crece a «N filtros ×» cuando hay filtros aplicados (Movimientos y Créditos); la × los quita todos de un toque.
- **Modo de rendimiento.** En Ajustes → Apariencia → Rendimiento se elige la frecuencia de actualización de la app: Automático (lo decide el sistema), 30, 60 o 120 Hz (120 solo si la pantalla lo admite). Menos Hz gastan menos batería; más Hz se ven más fluidos. Es una preferencia del teléfono y no viaja en las copias de seguridad.
- **Balance, Ingresos y Gastos en una sola fila** en Movimientos, para dejar más espacio a la lista. Las cifras se ajustan de tamaño si no caben.
- **Animaciones homologadas.** Las entradas, salidas, cambios de lugar de las listas, transiciones entre pantallas y conteos usan el mismo esquema de resortes de Material, sin duraciones fijas.

### Correcciones
- **Parpadeo al abrir la búsqueda.** El campo de búsqueda movía por un instante la pantalla hacia la pestaña de al lado (Compose intentaba traerlo a la vista mientras crecía). Ahora el campo se mide al ancho de la animación y aparece cuando ya cabe.
- **Nuevo movimiento vuelve a abrir en el concepto.** El foco, el teclado con mayúscula inicial y el botón ✓ regresan. Mientras el teclado del sistema está abierto, el teclado numérico se pliega y reaparece al pulsar ✓ o tocar el monto, sin empujar el botón «Guardar».

### Rendimiento
- Se apagó el desenfoque progresivo del encabezado y de la barra flotante: en pruebas llevaba el desplazamiento a ≈ 60 fps (16 ms por cuadro) frente a 9 ms sin él, y reducir el radio no cambiaba nada. Ahora el contenido se atenúa con un velo del color de fondo y el desplazamiento queda en 11-12 ms por cuadro.

### Interno
- `ExpandingFilterButton`, `MinicashTransitions`, `minicashAnimateItem()` y `HeaderCollapse`.
- Tipo de compilación `perf` (R8, firma debug, paquete `.perf`) para medir rendimiento sin tocar la app real.
- No cambia el formato de datos.

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
