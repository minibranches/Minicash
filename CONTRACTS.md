# Reglas Inmutables de Desarrollo - Minicash

* **Regla 1 (Montos):** Long en centavos en toda la base de datos y ViewModels (1000L = $10.00). Prohibido Double/Float para dinero.
* **Regla 2 (Transferencias):** Una sola fila con `type = "TRANSFER"`, `sourceAccountId` y `destinationAccountId`. Prohibido registros dobles pareados.
* **Regla 3 (Visibilidad):** `isVisibleInHome` solo filtra el carrusel de Inicio; los formularios listan todas las cuentas.
* **Regla 4 (Listas Segmentadas):** Usar siempre `rememberSegmentedShape(index, count)` para grupos de tarjetas.
* **Regla 5 (Navegación Flotante):** Prohibido usar `Modifier.weight` o `TooltipBox` dentro de la barra flotante. Prohibido añadir fondos opacos detrás de la cápsula; se permite un velo de color sobre el contenido (ver DESIGN.md §6). El desenfoque progresivo nativo (`Modifier.blur` con `BlurRadiusSpec`) está apagado por rendimiento; solo se reactiva tras medirlo con el build `perf`.
* **Regla 6 (Tipografía y Texto):** Quicksand local con `tnum` obligatorio para cifras. Sentence case estricto en español en toda la interfaz.
* **Regla 7 (Base de Datos):** MinicashDatabase v3 con migraciones explícitas. Prohibido `fallbackToDestructiveMigration`.
