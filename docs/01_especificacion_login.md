# Inicio de sesión de Angur Gestión

**Alcance:** primera etapa para la aplicación web y la aplicación Android. Ambas consultan el mismo servicio Java y la misma tabla MySQL. Después de autenticar, muestran un menú de muestra cuyos módulos son solo visuales; no implementan inventario, cotizaciones, ventas ni acciones posteriores al acceso.

**Fuentes del proyecto:** `Documento de la empresa.docx` (requisitos de seguridad y sistema), `VELIAN - Presentacion del equipo.pptx` (RF/RNF y tecnología), `Look & Feel AngurGestion.pdf` (pantalla de login y guía visual), y los cinco perfiles definidos para el proyecto.

## 1. Requisitos funcionales

| ID | Requisito | Criterio de aceptación | Origen |
| --- | --- | --- | --- |
| LOGIN-F01 | Permitir que administrador, vendedor, cliente, supervisor de ventas y encargado de almacén ingresen con usuario o correo y contraseña. | Una cuenta activa con credenciales correctas abre el menú visual y muestra el nombre y perfil. | Control de acceso RNF-03; perfiles del proyecto |
| LOGIN-F02 | Validar los dos campos antes de enviar la solicitud. | Si alguno está vacío, se marca el campo faltante, se indica que deben completarse usuario y contraseña y no se llama a la API. La contraseña no se modifica ni se recorta. | Usabilidad RNF-01 |
| LOGIN-F03 | Verificar las credenciales en el servidor contra MySQL. | El servidor consulta por usuario o correo mediante una consulta parametrizada y compara la contraseña con el hash almacenado. | Seguridad RNF-03; MySQL RS-08 |
| LOGIN-F04 | Rechazar credenciales incorrectas o cuentas inactivas. | En ambos casos se muestra el mismo mensaje genérico y no se inicia sesión. | Seguridad RNF-03 |
| LOGIN-F05 | Crear una sesión web al autenticar correctamente. | La sesión anterior se invalida y la nueva identifica al usuario y su perfil; la cookie es HttpOnly. | Control de acceso RNF-03 |
| LOGIN-F06 | Presentar mensajes de validación claros en web y Android. | La pantalla distingue campos vacíos, credenciales incorrectas y servicio no disponible sin revelar si existe la cuenta. | Usabilidad RNF-01; seguridad RNF-03 |
| LOGIN-F07 | Mostrar un menú visual después de iniciar sesión correctamente. | Se muestra nombre, perfil y tarjetas de módulos sin botones, enlaces ni acciones. | Alcance de la entrega |

## 2. Requisitos no funcionales

| ID | Requisito | Verificación |
| --- | --- | --- |
| LOGIN-N01 | Contraseñas almacenadas como PBKDF2-HMAC-SHA256 con sal aleatoria e iteraciones por cuenta. Nunca se guardan ni registran en texto plano. | Inspección de tabla y código; prueba de contraseña correcta e incorrecta. |
| LOGIN-N02 | Comunicación HTTPS en producción. Para probar en el emulador, el perfil `debug` permite HTTP solo hacia el servidor local del equipo. | Inspección de configuración Android y despliegue. |
| LOGIN-N03 | Interfaz legible, adaptable, con etiquetas persistentes, foco visible y errores comunicados con texto. | Revisión de pantalla web y Android. |
| LOGIN-N04 | La respuesta común debe cumplir el objetivo de 4 segundos de la presentación en el entorno de referencia del proyecto. | Medición de los casos funcionales bajo red local. |
| LOGIN-N05 | La API devuelve el mismo resultado de autenticación para web y Android. | Ejecutar los tres escenarios desde ambas plataformas. |

## 3. Actores y caso de uso

Los cinco perfiles son actores de **CU-LOGIN-01 Iniciar sesión**. Todos siguen el mismo flujo; el perfil solo determina la identidad devuelta y los permisos que se usarán en etapas posteriores.

### CU-LOGIN-01 Iniciar sesión

**Precondiciones:** la persona tiene una cuenta activa y el servicio de autenticación está disponible.

**Flujo principal:**

1. El usuario abre la pantalla de inicio de sesión.
2. Escribe usuario o correo y contraseña.
3. La interfaz comprueba que ambos campos tengan contenido.
4. La interfaz envía las credenciales al servicio.
5. El servicio busca la cuenta y verifica la contraseña.
6. El servicio confirma la autenticación, indica nombre y perfil; en web crea una sesión.
7. La interfaz muestra el menú visual con el nombre y el perfil autenticados.

**Alternativa A — campos vacíos:** en el paso 3, la interfaz marca los campos faltantes, muestra un mensaje y no envía la solicitud.

**Alternativa B — credenciales incorrectas o cuenta inactiva:** en el paso 5, el servicio responde `401` con un mensaje genérico; la interfaz lo presenta y permite corregir los datos.

**Alternativa C — servicio no disponible:** en el paso 4 o 5, la interfaz informa que no pudo conectarse; no muestra una confirmación de acceso.

**Poscondición de éxito:** la identidad y el perfil quedan disponibles en la respuesta; en web existe una sesión y se muestra el menú visual. **Poscondición de rechazo:** no se crea sesión.

## 4. Diagramas UML

El [atlas completo](../entregables/diagramas/INDICE.md) incluye casos de uso, actividad, secuencias web y Android, clases, componentes, despliegue, estados y modelo de datos de esta etapa. Las fuentes PlantUML son editables y las vistas SVG se pueden consultar sin herramientas adicionales. Los diagramas identificados con `P` modelan requisitos futuros y están separados de los identificados con `A`, que reflejan el código actual.

## 5. Prototipos de interfaz

La web y Android toman del PDF los colores verde azulado, fondo claro y botón coral. La pantalla inicial muestra marca Angur Gestión, saludo, usuario o correo, contraseña, botón de acceso y mensajes. Después de una autenticación correcta se muestra un menú de ejemplo con Productos, Inventario, Clientes, Cotizaciones, Ventas y Reportes. Las tarjetas no son controles y no ejecutan acciones.

- Web: `web/src/main/webapp/index.html` y `web/src/main/webapp/assets/login.css`.
- Android: `android/app/src/main/res/layout/activity_login.xml` y sus recursos de color/dibujo.

Las capturas de los prototipos y pruebas se documentan en [02_pruebas_login.md](02_pruebas_login.md).

## 6. Modelo de datos

`db/schema.sql` crea únicamente la tabla `usuarios`, necesaria para esta etapa. Su columna `rol` admite exactamente los cinco perfiles definidos. `clave_hash` almacena el resultado PBKDF2; no existe columna para contraseña en texto plano. La creación inicial de cuentas se realiza con una utilidad local de desarrollo, sin formulario de registro.

## 7. Decisiones de esta etapa

- La autenticación acepta **usuario o correo** en un mismo campo.
- Android muestra el menú de ejemplo después del login, pero no conserva una sesión para otros módulos, pues aún no existen en esta entrega.
- La URL `10.0.2.2` se utiliza solo en el emulador Android para llegar a Tomcat en el equipo anfitrión. Un dispositivo físico requiere una URL de servidor accesible y HTTPS.
- El menú posterior al login es una maqueta visual. Los módulos no tienen interacción ni lógica de negocio.

## 8. Contrato de autenticación compartido

`POST /angur-web/api/auth/login` recibe `application/x-www-form-urlencoded` con `identifier` (usuario o correo) y `password`. La respuesta es JSON:

| Estado | Respuesta | Uso en la interfaz |
| --- | --- | --- |
| `200` | `{"ok":true,"name":"...","role":"..."}` | Mostrar el menú visual, nombre y perfil. |
| `400` | `{"ok":false,"code":"REQUIRED_FIELDS" o "INVALID_INPUT","message":"..."}` | Marcar o explicar datos incompletos o inválidos. |
| `401` | `{"ok":false,"code":"INVALID_CREDENTIALS","message":"..."}` | Informar rechazo genérico. |
| `503` | `{"ok":false,"code":"SERVICE_UNAVAILABLE","message":"..."}` | Indicar que el servicio no está disponible. |

La web conserva la cookie de sesión `HttpOnly`. La aplicación Android no la reutiliza en esta etapa. La configuración de producción debe usar HTTPS y un host real; la URL local de depuración se limita al emulador.

