# Cambios

Las versiones siguen [SemVer](https://semver.org/lang/es/). Cada versión se publica como *release* en GitHub con su tag `vX.Y.Z`.

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
