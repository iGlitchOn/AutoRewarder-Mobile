# AutoRewarder Mobile

AutoRewarder Mobile acompaña a AutoRewarder PC cuando necesitas las funciones de teléfono de Microsoft Rewards. Se vincula por QR o código manual, recibe acciones del PC y devuelve el estado de la actividad.

> Versión documentada: **4.3.29**<br>
> Este repositorio: [iGlitchOn/AutoRewarder-Mobile](https://github.com/iGlitchOn/AutoRewarder-Mobile)<br>
> Compañero del fork [iGlitchOn/AutoRewarder-PC](https://github.com/iGlitchOn/AutoRewarder-PC)<br>
> Proyecto original: [safarsin/AutoRewarder](https://github.com/safarsin/AutoRewarder)

## Origen

AutoRewarder PC es un **fork** de [safarsin/AutoRewarder](https://github.com/safarsin/AutoRewarder). El original emula el móvil dentro de Edge; **no incluye una app Android**.

Este repositorio es código propio de la distribución iGlitchOn: una app nativa que se vincula con [AutoRewarder-PC](https://github.com/iGlitchOn/AutoRewarder-PC) y ejecuta en el teléfono las acciones que Rewards pide en dispositivo móvil.

### Cambios propios (esta app no existe en el original)

- App Android nativa (`off.iglitch.autorewarder`) con WebView de la interfaz `phone.html`.
- Vinculación con el PC por QR o código manual (`QrScanActivity`).
- Puente JS–Android (`AndroidJs`) para cámara, Bing, instalación de APK y estado de red.
- Check-in, noticias y búsquedas móviles pedidas por el PC (`BingTasks`, `BingLauncher`).
- Consulta de releases de PC y móvil; descarga e instalación solo de APK propios. Las versiones de `safarsin/AutoRewarder` se notifican y no se instalan.

## Para qué sirve

La aplicación puede:

- Vincularse con AutoRewarder PC mediante QR o código manual.
- Ejecutar búsquedas móviles solicitadas por el PC.
- Iniciar o detener una ejecución remota.
- Abrir Bing o Edge cuando el flujo lo necesita.
- Solicitar check-in y noticias cuando Rewards los ofrece para la cuenta.
- Mostrar el estado de la conexión y el progreso comunicado por el PC.
- Consultar las releases de PC y móvil.
- Descargar una actualización propia y abrir el instalador de Android.

Bing y Rewards siguen siendo los servicios que deciden qué tareas existen y qué puntos se acreditan. Una tarea puede desaparecer aunque la aplicación esté bien vinculada.

## Licencia

MIT. La app Android es © iGlitchOn (2026). La UI y el protocolo derivan del fork PC de safarsin/AutoRewarder (MIT, © 2025 safarsin). Textos: [LICENSE](LICENSE) y [NOTICE](NOTICE). ZXing y AndroidX son Apache-2.0.

## Disclaimer

No está afiliado a Microsoft. Automatizar Bing o Microsoft Rewards puede violar sus términos. El uso, la cuenta y los puntos son responsabilidad de quien instala la app.

## Requisitos

- Android 8.0 o posterior, API 26 como mínimo.
- AutoRewarder PC instalado y abierto.
- Wi-Fi local para la vinculación normal.
- Internet.
- Cámara para escanear el QR, salvo que uses el código manual.
- Bing instalado si vas a usar los botones que lo abren directamente.

## Descargar e instalar

1. Abre [Releases de AutoRewarder Mobile](https://github.com/iGlitchOn/AutoRewarder-Mobile/releases).
2. Descarga `AutoRewarder-Mobile-X.Y.Z.apk` desde una release oficial.
3. Abre el archivo desde **Descargas**.
4. Si Android lo solicita, entra en Ajustes y activa **Permitir desde esta fuente** solo para el navegador o gestor de archivos que usaste.
5. Regresa al instalador y confirma.
6. Mantén Play Protect activo y acepta el análisis si Android lo ofrece.
7. Abre AutoRewarder Mobile.

El APK se distribuye fuera de Google Play, así que Android puede mostrar una advertencia o pedir autorización para instalarlo desde esa fuente. No desactives Play Protect de manera global ni instales copias de origen desconocido.

## Permisos

### Cámara

Se usa para leer el QR de vinculación. Android solicita este permiso al pulsar **Scan QR**.

### Red

Internet, el estado de red y la información Wi-Fi permiten hablar con el PC, descubrirlo en la red local y consultar releases.

### Instalación de actualizaciones

`REQUEST_INSTALL_PACKAGES` solo permite abrir el instalador de Android para un APK descargado. Android y el usuario deben confirmar la instalación final. La aplicación no instala actualizaciones en silencio.

La aplicación no solicita contactos, ubicación, micrófono, SMS ni almacenamiento amplio.

## Vinculación por QR

### En el PC

1. Abre AutoRewarder PC.
2. Selecciona la cuenta correcta.
3. Ve a **Account > Vincular un celular**.
4. Deja el QR visible en la pantalla.

### En el teléfono

1. Abre AutoRewarder Mobile.
2. Pulsa **Scan QR**.
3. Acepta la cámara.
4. Apunta al QR completo y espera la confirmación.

La cámara conserva la proporción de la imagen para que el código no se vea estirado. Si no enfoca, prueba el código manual:

1. Pulsa **Manual code**.
2. Escribe el código que muestra el PC.
3. Pulsa **Pair**.

## Botones principales

### Conexión

- **Scan QR** abre el lector de cámara.
- **Manual code** permite vincular sin cámara.
- **Pair** confirma el código escrito.
- **Unlink phone** elimina la relación con el PC.

### Actividad

- **Check-in** solicita el check-in si está disponible.
- **News** solicita la actividad de noticias si Rewards la muestra.
- **Install Bing / Login Bing** abre el flujo correspondiente.
- **Start searches** inicia las búsquedas móviles configuradas.
- **Stop** detiene la ejecución activa sin desvincular el teléfono.

### Control del PC

- **Start PC** solicita una ejecución en el PC.
- **Tasks only** solicita únicamente las tareas diarias.
- **Open Edge** abre Edge en el PC si el puente está conectado.
- **Check updates** consulta las releases de ambos repositorios.
- **Download** descarga una actualización propia.
- **Cancel** cierra el diálogo sin cambiar la instalación.

Un botón puede quedar deshabilitado si no hay teléfono vinculado, el PC está apagado, la cuenta no está disponible o la tarea ya terminó. En ese caso la interfaz evita enviar una acción que no tiene dónde ejecutarse.

## Primera prueba

1. Conecta PC y teléfono a la misma Wi-Fi.
2. Vincula el teléfono.
3. Comprueba que la cuenta es la misma en ambos lados.
4. Ejecuta pocas búsquedas con el navegador visible.
5. Revisa el estado en PC y móvil.
6. Usa **Stop** para detener la sesión manualmente.

No cierres la aplicación mientras espera respuesta del PC. Si la cerraste, vuelve a abrirla y comprueba el estado antes de iniciar otra sesión.

## Actualizaciones

La aplicación consulta:

- [AutoRewarder-Mobile Releases](https://github.com/iGlitchOn/AutoRewarder-Mobile/releases)
- [AutoRewarder-PC Releases](https://github.com/iGlitchOn/AutoRewarder-PC/releases)

Solo las releases propias se pueden descargar desde el botón de actualización. Las versiones del repositorio original se notifican como referencia y no se instalan automáticamente.

Para actualizar:

1. Pulsa **Check updates**.
2. Revisa la versión encontrada.
3. Pulsa **Download** o **Cancel**.
4. Espera a que termine la descarga.
5. Acepta el instalador de Android.
6. Si Android vuelve a pedir autorización para esa fuente, comprueba que reconoces el archivo antes de aceptarla.

## Si algo falla

### No aparece el PC

Comprueba que ambos dispositivos estén en la misma red. Desactiva temporalmente VPN o red de invitados, revisa Windows Firewall y prueba el código manual.

### El QR no se lee

Limpia la cámara, sube el brillo del monitor, evita reflejos, encuadra el código completo y prueba a acercar o alejar lentamente el teléfono.

### Check-in o noticias no funcionan

Estas acciones dependen de que Microsoft las ofrezca para la cuenta y de que el teléfono siga conectado. La aplicación no puede crear una actividad que Rewards no muestra.

### Bing no inicia sesión

La sesión es la de Rewards dentro de esta app, no un botón guardado. Si el aviso dice que Bing está desactivada, actívala en Ajustes. Si no hay sesión, pulsa **Iniciar sesión en Bing** y entra con la cuenta Microsoft hasta que diga «Bing: sesión lista».

### Se desvinculó el teléfono

La cuenta del PC debe conservarse. En el PC confirma la cuenta activa y revisa el estado de la ejecución. Check-in y noticias pueden requerir volver a vincular el teléfono.

## Compilación

El proyecto Android usa Gradle. Desde la carpeta `android`:

```powershell
.\gradlew.bat assembleRelease
```

La compilación copia desde `gui/` los archivos de la interfaz móvil. Para firmar un APK de release necesitas un `keystore.properties` local. No subas contraseñas, keystores ni archivos de firma.

## Seguridad y privacidad

- No compartas QR, códigos manuales, perfiles de Edge ni capturas con sesiones abiertas.
- Mantén Android, Windows, Edge y Bing actualizados.
- Revisa los permisos desde Ajustes de Android.
- Descarga solo desde los repositorios oficiales.

## Aviso

AutoRewarder interactúa con servicios de terceros. Microsoft puede cambiar Rewards, limitar funciones o restringir la automatización según sus términos. El usuario es responsable de su cuenta y del uso de la aplicación.

## Reportar un problema

Incluye la versión, modelo y versión de Android, versión de AutoRewarder PC, tipo de conexión y el paso que falló. No incluyas contraseñas, tokens, QR ni perfiles de navegador.

Reporta errores en [Issues de AutoRewarder-Mobile](https://github.com/iGlitchOn/AutoRewarder-Mobile/issues).
