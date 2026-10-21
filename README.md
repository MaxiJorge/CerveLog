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
| RF1 · Autenticación | Firebase Authentication con email y contraseña |
| RF2 · Listado | Solo las cervezas del usuario, en tiempo real y ordenadas por fecha |
| RF3 · Detalle | Foto, puntuación, datos, mapa y notas |
| RF4 · Creación y edición | Un formulario para crear y editar, guardado en Firestore. También se puede eliminar una cerveza |
| RF5 · Geolocalización | Ubicación actual con FusedLocationProvider, o tocando el mapa para elegir otro punto. Se muestra en Google Maps y se guarda con cada cerveza |
| RF6 · Cámara | Foto con la cámara o desde la galería |
| RF7 · Notificaciones | Notificación local cuando otro usuario registra una cerveza cerca |
| Opcional · Funcionalidad adicional | Mapa comunitario con carga por zona visible, agrupado de marcadores y detalle de la cerveza al tocar un marcador, y perfil con estadísticas |

## Tecnologías

Kotlin · Jetpack Compose (Material 3) · Navigation Compose · MVVM con StateFlow y corrutinas · Firebase Authentication · Cloud Firestore · Google Play Services Location · Google Maps (maps-compose).

## Estructura

```
app/src/main/java/com/jorge/cervelog
├── data/model          Cerveza y Publicacion
├── data/repository     Acceso a Firebase, ubicación y fotos
├── notificaciones      Aviso de cervezas cercanas (RF7)
└── ui                  Pantallas (auth, cervezas, mapa, perfil), componentes, navegación y tema
firestore.rules         Reglas de seguridad de Firestore
```

## Requisitos

- Android Studio Hedgehog (2023.1.1) o posterior. Se ha desarrollado con AGP 9.4 y JDK 17.
- Emulador o dispositivo con **API 26 o superior** y **Google Play Services** (imagen del sistema «Google APIs» o «Google Play»).

## Configuración y ejecución

El repositorio no incluye `google-services.json` ni la API key de Google Maps porque contienen claves. Hay dos formas de obtenerlos:

- **Opción rápida:** pedir al grupo el `google-services.json` y la API key del proyecto de Firebase que ya está configurado (`cervelog-a67a6`, con usuarios y datos de ejemplo). Después, seguir desde el paso 5.
- **Opción completa:** crear un proyecto propio con los pasos 1 a 7.

1. **Crear el proyecto de Firebase.** En la [consola de Firebase](https://console.firebase.google.com), pulsa «Añadir proyecto» (Google Analytics no hace falta).
2. **Registrar la app Android.** Usa el nombre de paquete `com.jorge.cervelog`. Descarga `google-services.json`.
3. **Activar Authentication.** En Authentication → Método de inicio de sesión, activa «Correo electrónico/contraseña».
4. **Crear Firestore y publicar las reglas.** En Firestore Database, crea la base de datos en modo producción. En la pestaña «Reglas», pega el contenido de [`firestore.rules`](firestore.rules) y pulsa «Publicar». También se puede hacer con `firebase deploy --only firestore:rules`.
5. **Copiar `google-services.json`** a la carpeta `app/` del proyecto.
6. **Configurar la API key de Google Maps.**
   - En la [consola de Google Cloud](https://console.cloud.google.com), dentro del mismo proyecto, activa **Maps SDK for Android**.
   - En «Credenciales», crea una API key.
   - Añádela al archivo `local.properties` de la raíz del proyecto (Android Studio lo crea al abrir el proyecto):
     ```properties
     MAPS_API_KEY=tu_api_key
     ```
     Gradle la inyecta en el `AndroidManifest.xml`. Sin ella, la app funciona, pero los mapas aparecen en gris.
7. **Ejecutar.** Abre el proyecto en Android Studio, espera a que sincronice Gradle y pulsa ▶ Run.

### Uso en el emulador

- **Ubicación:** el emulador no tiene GPS real. Fija la posición en Extended Controls (⋯) → Location → «Set location», o con `adb emu geo fix <longitud> <latitud>` (por ejemplo, `adb emu geo fix -58.4245 -34.5890` para Palermo, Buenos Aires).
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
