# Minicash: guía para Claude

App Android de finanzas personales, 100% local (Kotlin, Jetpack Compose, Material 3 Expressive, Room). Repositorio público bajo licencia MIT. El dueño del producto (PO/BA) decide qué se construye; aquí se implementa y se mantiene.

## Lee antes de tocar código
- `CONTRACTS.md`: reglas inmutables (montos en `Long` centavos, transferencias en una sola fila, migraciones de Room explícitas, etc.). No se rompen sin acuerdo explícito.
- `DESIGN.md`: decisiones de diseño y arquitectura por capas.
- `docs/hoja-de-ruta.md`: ideas futuras. No se implementan sin que se pidan.
- Idioma de la interfaz y de los commits: español, "sentence case".

## Comandos
```bash
./gradlew :app:testDebugUnitTest     # pruebas unitarias
./gradlew :app:assembleDebug         # APK de depuración
./gradlew :app:assembleRelease       # APK de release firmado (ver Firma)
```
Un compile de release tarda varios minutos (R8): ejecútalo en segundo plano o con timeout alto.

## Flujo para un bug o mejora
1. Reproduce o localiza la causa leyendo el código; di la causa antes de corregir.
2. Corrige con el cambio mínimo y respeta `CONTRACTS.md`.
3. Si tocas la base de datos: sube la versión de `MinicashDatabase`, escribe la migración y revisa el esquema exportado en `app/schemas`. Nunca `fallbackToDestructiveMigration`.
4. Corre `:app:testDebugUnitTest`. Agrega una prueba si el bug se puede cubrir.
5. Si el cambio es visual, verifícalo en el teléfono con adb (instalar, abrir la pantalla, captura). Antes de instalar encima de una copia con datos, confirma que exista una copia de seguridad JSON.
6. Commit en español, mensaje breve y claro de qué cambia y por qué. No hagas commit, push ni release si no se pide.

## Firma
- La llave de release **no está en el repositorio**. El build lee `keystore.properties` (en la raíz, ignorado por git) que apunta a un `.jks` fuera del repo.
- Nunca leas, imprimas, copies ni subas el `.jks` ni las contraseñas. Nunca los agregues al repo.
- Toda versión debe firmarse con **la misma llave**. Si falta `keystore.properties`, el release cae a la llave debug: eso produce un APK que no actualiza a los publicados. En ese caso, detente y avisa en lugar de publicar.
- Para comprobar la firma: `apksigner verify --print-certs <apk>`. Debe aparecer `CN=Minicash, O=minibranches`, no `Android Debug`.
- Instalar un APK con otra firma sobre uno ya instalado exige desinstalar y borra los datos del teléfono.

## Versiones y releases
- SemVer: `1.0.x` correcciones, `1.x.0` funciones nuevas, `x.0.0` cambios que rompen compatibilidad de datos.
- En `app/build.gradle.kts` actualiza **`versionName` y también `versionCode`** (debe subir siempre; Android no actualiza con un `versionCode` igual o menor).
- Publicar solo cuando el dueño lo pida expresamente:
  1. Pruebas unitarias en verde y árbol limpio, rama `main` sincronizada con `origin`.
  2. Compilar release firmado y verificar la firma.
  3. Nombrar el APK `Minicash-X.Y.Z.apk`.
  4. `gh release create vX.Y.Z Minicash-X.Y.Z.apk --title "Minicash X.Y.Z" --notes-file <notas>`; notas en español: qué cambia para el usuario.
  5. Actualiza la versión mencionada en la sección "Estado" del `README.md`.
- Si cambió el formato de datos, indícalo en las notas y recomienda hacer copia de seguridad.

## Privacidad del repositorio (es público)
- No incluyas secretos, rutas personales, correos ni datos reales de finanzas en código, docs, capturas o mensajes de commit.
- Los commits usan el correo noreply de GitHub configurado en el repo; no lo cambies.
- No versiones `.claude/`, APK/AAB, `local.properties` ni nada de `.gitignore`.
- Material de terceros solo con licencia compatible; la música y el video promocional son propios (`docs/promo/`).
