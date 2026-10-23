# 🍺 CerveLog

Trabajo Integrador · Módulo 2 · Aplicaciones Móviles

**Grupo:** _[nombre del grupo]_

**Integrantes:**
- Jorge Ortiz de Mendívil Vicente
- _[integrante 2]_
- _[integrante 3]_

## Descripción

CerveLog es una app Android nativa para llevar un registro personal de las cervezas que pruebas. De cada una guardas el nombre, la cervecería, el estilo, el ABV, el IBU, una puntuación de 1 a 5 estrellas, las notas de cata, el lugar, la ubicación en el mapa y una foto.

Además, las cervezas con ubicación aparecen en un **mapa comunitario** donde ves lo que han probado otros usuarios cerca de ti, y la app te **avisa con una notificación** cuando alguien registra una cerveza a menos de 5 km.

| Requisito | Cómo se cumple |
|---|---|
| RF1 · Autenticación | Firebase Authentication con email y contraseña (con correo de confirmación) o con Google |
| RF2 · Listado | Solo las cervezas del usuario, en tiempo real y ordenadas por fecha |
| RF3 · Detalle | Foto, puntuación, datos, mapa y notas |
| RF4 · Creación y edición | Un formulario para crear y editar, guardado en Firestore. También se puede eliminar una cerveza |
| RF5 · Geolocalización | Ubicación actual con FusedLocationProvider, o tocando el mapa para elegir otro punto. Se muestra en Google Maps y se guarda con cada cerveza |
| RF6 · Cámara | Foto con la cámara o desde la galería |
| RF7 · Notificaciones | Notificación local cuando otro usuario registra una cerveza cerca |
| Opcional · Funcionalidad adicional | Mapa comunitario con el nombre de cada cerveza, filtros (puntuación mínima, estilo y graduación), carga por zona visible y agrupado de marcadores. Cada cerveza tiene un detalle con la ruta por calles desde tu ubicación (Routes API) y un botón para abrirla en Google Maps. Perfil con estadísticas y modo oscuro (sistema, claro u oscuro). Icono propio de la app |

## Tecnologías

Kotlin · Jetpack Compose (Material 3) · Navigation Compose con rutas tipadas · MVVM con StateFlow y corrutinas sobre arquitectura limpia · Firebase Authentication · Cloud Firestore · Google Play Services Location · Google Maps (maps-compose) · Routes API de Google · JUnit + kotlinx-coroutines-test.

## Estructura

Arquitectura limpia en tres capas: la interfaz y los datos dependen del dominio, y el dominio no depende de nada.

```
domain/                 Módulo Kotlin puro: modelos, AppResult/DomainError, interfaces de repositorio y casos de uso
app/src/main/java/com/jorge/cervelog
├── di                  AppContainer: crea los repositorios y los casos de uso
├── data                Documentos de Firestore, mappers y repositorios (Firebase, ubicación, fotos y rutas)
├── notificaciones      Aviso de cervezas cercanas (RF7)
└── ui                  Pantallas (auth, cervezas, mapa, perfil), componentes, navegación y tema
firestore.rules         Reglas de seguridad de Firestore
```

## Tests

```bash
./gradlew test
```

Ejecuta los tests unitarios del dominio y de los ViewModels en la JVM, sin emulador ni Firebase.

## Requisitos

- Android Studio Hedgehog (2023.1.1) o posterior. Se ha desarrollado con AGP 9.4 y JDK 17.
- Emulador o dispositivo con **API 26 o superior** y **Google Play Services** (imagen del sistema «Google APIs» o «Google Play»).

## Configuración y ejecución

El repositorio no incluye `google-services.json` ni la API key de Google Maps porque contienen claves. Hay dos formas de obtenerlos:

- **Opción rápida:** pedir al grupo el `google-services.json` y la API key del proyecto de Firebase que ya está configurado (`cervelog-a67a6`, con usuarios y datos de ejemplo). Después, seguir desde el paso 5.
- **Opción completa:** crear un proyecto propio con los pasos 1 a 7.

1. **Crear el proyecto de Firebase.** En la [consola de Firebase](https://console.firebase.google.com), pulsa «Añadir proyecto» (Google Analytics no hace falta).
2. **Registrar la app Android.** Usa el nombre de paquete `com.jorge.cervelog`. Descarga `google-services.json`.
3. **Activar Authentication.** En Authentication → Método de inicio de sesión, activa «Correo electrónico/contraseña» y «Google» (elige un correo de asistencia). El correo de confirmación del registro lo envía Firebase; no hace falta configurar nada más.
   Para que funcione «Continuar con Google», registra la huella SHA-1 de tu clave de depuración en Configuración del proyecto → Tus apps → Añadir huella digital. Cada ordenador tiene su propia clave, así que cada integrante debe añadir la suya. Para verla:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
   ```
   (o en Android Studio: Gradle → app → Tasks → android → signingReport). **Después de activar Google y añadir la huella, vuelve a descargar `google-services.json`**: el nuevo incluye el cliente OAuth que necesita la app. Sin él, el botón muestra «El inicio de sesión con Google no está configurado en Firebase».
4. **Crear Firestore y publicar las reglas.** En Firestore Database, crea la base de datos en modo producción. En la pestaña «Reglas», pega el contenido de [`firestore.rules`](firestore.rules) y pulsa «Publicar». También se puede hacer con `firebase deploy --only firestore:rules`.
5. **Copiar `google-services.json`** a la carpeta `app/` del proyecto.
6. **Configurar la API key de Google Maps.**
   - En la [consola de Google Cloud](https://console.cloud.google.com), dentro del mismo proyecto, activa **Maps SDK for Android** y **Routes API**.
   - En «Credenciales», crea una API key. Si le pones restricción de APIs, incluye las dos.
   - Añádela al archivo `local.properties` de la raíz del proyecto (Android Studio lo crea al abrir el proyecto):
     ```properties
     MAPS_API_KEY=tu_api_key
     ```
     Gradle la inyecta en el `AndroidManifest.xml` (para el mapa) y en `BuildConfig` (para pedir las rutas). Sin ella, la app funciona, pero los mapas aparecen en gris. Sin la Routes API, el detalle del mapa no dibuja la ruta, pero el botón «Abrir en Google Maps» sigue funcionando.
7. **Ejecutar.** Abre el proyecto en Android Studio, espera a que sincronice Gradle y pulsa ▶ Run.

### Uso en el emulador

- **Ubicación:** el emulador no tiene GPS real. Fija la posición en Extended Controls (⋯) → Location → «Set location», o con `adb emu geo fix <longitud> <latitud>` (por ejemplo, `adb emu geo fix -58.4245 -34.5890` para Palermo, Buenos Aires).
- **Google:** el emulador necesita una imagen con Google Play y una cuenta de Google añadida en Ajustes → Cuentas.
- **Cámara:** la cámara del emulador muestra una escena virtual. Para probar la galería, arrastra una imagen a la ventana del emulador.

### Usuarios de ejemplo

Solo existen en el proyecto `cervelog-a67a6`:

| Email | Contraseña |
|---|---|
| ana@aplicaciones.com | cervelog123 |
| carlos@aplicaciones.com | cervelog123 |
| lucia@aplicaciones.com | cervelog123 |
| mikel@aplicaciones.com | cervelog123 |

### Probar la notificación

La notificación aparece cuando **otro usuario** registra una cerveza **con ubicación a 5 km o menos** de tu posición, con tu sesión iniciada y la app abierta.

1. Inicia sesión, abre la pestaña **Mapa** y fija la ubicación del emulador (debe aparecer el punto azul).
2. Desde otro dispositivo, inicia sesión con otro usuario y registra una cerveza cerca. Con un solo dispositivo, usa el script de prueba, que publica como un usuario de ejemplo:
   ```
   python demo/publicar.py app/google-services.json ana "Antares Scotch" "Scotch Ale" 5 "Palermo" -34.5855 -58.4300
   ```
3. En unos segundos aparece «🍺 Nueva cerveza a X km».

## Seguridad

- Cada usuario solo puede leer y escribir en `usuarios/{su uid}/...`.
- El mapa (`publicaciones`) lo puede leer cualquier usuario con sesión, pero cada uno solo crea, modifica y borra sus propias publicaciones.
- Todo lo demás está denegado. Las reglas completas están en [`firestore.rules`](firestore.rules).
