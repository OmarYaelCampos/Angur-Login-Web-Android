# Pruebas de inicio de sesión

**Fecha:** 14 de octubre de 2026. **Entorno:** Chrome de escritorio, emulador Android `Small_Phone` (720 × 1280), Tomcat 10.1, MySQL 8.0 en una instancia temporal aislada. Ambas plataformas llamaron al mismo servlet y consultaron la misma tabla `usuarios`. Se prepararon cinco cuentas de práctica con contraseña temporal; no se incluye esa base de prueba en la entrega.

## Casos funcionales solicitados

| ID | Plataforma | Entrada | Resultado esperado | Resultado observado | Captura |
| --- | --- | --- | --- | --- | --- |
| WEB-01 | Web | `cliente` y clave correcta | Confirmación con nombre y rol `CLIENTE` | **Aprobado:** respuesta `200`, nombre y rol visibles | [PNG](../evidencias/web/01_credenciales_validas.png) |
| WEB-02 | Web | `cliente` y clave incorrecta | Rechazo sin revelar existencia de la cuenta | **Aprobado:** respuesta `401`, mensaje genérico visible | [PNG](../evidencias/web/02_credenciales_incorrectas.png) |
| WEB-03 | Web | Ambos campos vacíos | Marcar campos y no enviar solicitud | **Aprobado:** se muestran los dos errores de obligatoriedad | [PNG](../evidencias/web/03_campos_vacios.png) |
| AND-01 | Android | `cliente` y clave correcta | Confirmación con nombre y rol `CLIENTE` | **Aprobado:** nombre y rol visibles tras respuesta `200` | [PNG](../evidencias/android/01_credenciales_validas.png) |
| AND-02 | Android | `cliente` y clave incorrecta | Mostrar mensaje genérico | **Aprobado:** se muestra “Usuario o contraseña incorrectos.” | [PNG](../evidencias/android/02_credenciales_incorrectas.png) |
| AND-03 | Android | Ambos campos vacíos | Marcar campos y no enviar solicitud | **Aprobado:** errores en ambos campos y aviso de obligatoriedad | [PNG](../evidencias/android/03_campos_vacios.png) |

Pantallas iniciales del diseño simplificado: [web](../evidencias/web/00_pantalla_login.png), [web con el tamaño de la referencia](../evidencias/web/00_login_referencia.png) y [Android](../evidencias/android/00_pantalla_login.png). El formulario Android admite desplazamiento vertical cuando aparecen mensajes o se usa una pantalla menor. Las capturas de los seis casos funcionales de la tabla se tomaron antes de simplificar la presentación; las validaciones y la API no cambiaron.

## Comprobaciones adicionales

- La API aceptó los cinco usuarios de práctica y devolvió respectivamente `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE`, `SUPERVISOR_VENTAS` y `ENCARGADO_ALMACEN` con código `200`.
- El correo `cliente@angur.local` también obtuvo `200` y rol `CLIENTE`.
- La API rechazó `identifier` y `password` vacíos con `400` y código `REQUIRED_FIELDS`.
- Tres pruebas unitarias del validador Android pasaron: ambos campos vacíos, identificador con solo espacios y campos completos.
- Se midieron seis solicitudes válidas consecutivas desde el anfitrión, entre **0.067 y 0.126 segundos**. Cumplen el objetivo de 4 segundos en este entorno local; no equivalen a una prueba de carga ni a una garantía para redes remotas.

## Alcance de la verificación

La variante Android probada fue `debug` contra el servicio local. La URL HTTPS de `release` es un marcador de posición y requiere un servidor real para una puesta en producción. No se probó un teléfono físico. La confirmación de acceso permanece en la pantalla de login porque los módulos posteriores están fuera de esta primera etapa.
