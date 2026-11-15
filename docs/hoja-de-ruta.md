# Hoja de ruta de Minicash

Ideas grandes que todavía no se construyen. Cada una dice qué implica, para decidir con información cuando toque.

## 1. Registro por foto de recibo
**Idea:** tomar una foto (o adjuntar una imagen) de un ticket, por ejemplo de Soriana, y que Minicash sugiera el movimiento para que el usuario solo confirme.
- **Cómo:** reconocimiento de texto en el dispositivo (ML Kit Text Recognition, sin enviar la imagen a ningún servidor) y un analizador que busque monto total, comercio y fecha.
- **Pantalla:** una hoja de revisión con los datos sugeridos y la categoría propuesta por el nombre del comercio; el usuario corrige y guarda.
- **Permisos:** cámara, solo al usar la función. Para adjuntar una imagen basta el selector de fotos del sistema, que no pide permisos.
- **Riesgos:** los tickets varían mucho; conviene empezar solo con el total y la fecha y marcar siempre lo detectado como sugerencia.

## 2. Sincronización con Google Drive
**Idea:** que los datos se respalden y se sincronicen entre dispositivos.
- **Cambia la promesa de privacidad:** hoy la app no tiene permiso de red. Esto exige `INTERNET`, inicio de sesión de Google y actualizar el README.
- **Opción chica, sin permiso de red:** guardar el respaldo JSON en Drive desde el selector de archivos, que ya funciona hoy.
- **Opción completa:** carpeta privada de la app en Drive (`appDataFolder`) con el respaldo cifrado. La sincronización entre dos teléfonos necesita resolver conflictos; con dos usuarios editando a la vez lo más simple es "gana el último cambio" por movimiento.

## 3. Lectura de notificaciones (como Cashew)
**Idea:** registrar movimientos a partir de las notificaciones de las apps del banco.
- **Cómo:** `NotificationListenerService`, que requiere que el usuario conceda el acceso a notificaciones en los ajustes del sistema.
- **Reglas:** por cada app bancaria, el paquete, palabras clave y una expresión para el monto y el comercio, asignadas a cuenta y categoría.
- **Flujo:** una bandeja "por confirmar" antes de registrar, nunca registrar solo.
- **Privacidad:** procesar en el dispositivo y no guardar el texto original de la notificación.

## 4. Registro por voz en lenguaje natural
**Idea:** decir "gasté 250 en súper con Nu" y obtener el movimiento listo para confirmar.
- **Cómo:** reconocimiento de voz del sistema (funciona sin red si el idioma está descargado) y un analizador de frases para monto, concepto, cuenta y categoría, apoyado en los nombres que ya existen en la app.
- **Pantalla:** botón de micrófono en "Agregar movimiento" que llena el formulario; el usuario revisa antes de guardar.
- **Permisos:** micrófono, solo al usar la función.

## Hecho desde la última revisión
- **Exportar a CSV** con periodo a elegir, desde Ajustes > Copias de seguridad.
- **Bloqueo de la app** con huella, rostro o PIN del sistema, sin permisos de red.
- **Detalle de cuenta, menú del botón de agregar y búsquedas recientes** en Movimientos.

## Ideas que se descartaron por ahora
- **Movimientos recurrentes** y **presupuesto por categoría**: se decidió no hacerlos todavía.
- **Transición compartida** de la tarjeta de cuenta al detalle: exigiría reestructurar la navegación entre las pestañas y las pantallas superpuestas.

## Orden sugerido
1. Foto de recibo, porque se usa todos los días y no necesita red.
2. Registro por voz, que comparte el analizador de frases con el punto anterior.
3. Respaldo en Drive por el selector de archivos, sin cambiar la privacidad.
4. Lectura de notificaciones y sincronización completa, que son lo más delicado.
