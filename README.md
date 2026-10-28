# Angur Gestión

Login para la versión web y Android. Ambas aplicaciones consultan la misma API Java y la misma tabla MySQL. Si falta el usuario o la contraseña, aparece un mensaje para completar ambos campos. Si la autenticación es correcta, se abre un menú de muestra. Sus módulos son únicamente visuales: todavía no tienen acciones.

## Requisitos

- Java JDK 17 o posterior.
- Apache Maven.
- MySQL 8.
- Apache Tomcat 10.1.
- Android Studio y Android SDK 36 para la aplicación móvil.

## Abrir la versión web

1. Inicia el servicio MySQL. En MySQL Workbench, abre y ejecuta una vez **db/schema.sql**.
2. En Workbench, con una cuenta administradora local, crea una cuenta para la app. Cambia el texto de contraseña antes de ejecutar:

   ```sql
   CREATE USER IF NOT EXISTS 'angur_app'@'127.0.0.1' IDENTIFIED BY 'REEMPLAZA_CON_TU_CLAVE_LOCAL';
   GRANT SELECT ON angur_gestion.usuarios TO 'angur_app'@'127.0.0.1';
   ```

   La app solo requiere permiso SELECT para validar el inicio de sesión. Si la cuenta ya existe, conserva su contraseña actual y úsala cuando el script la solicite.
3. Para abrir la web, haz doble clic en **Abrir Angur Web.bat** en la carpeta raíz del repositorio. Se abrirá una ventana de PowerShell, compilará el proyecto y pedirá el usuario y contraseña de la cuenta `angur_app` de MySQL. Deja esa ventana abierta mientras uses el sistema.
4. Visita **http://localhost:8080/angur-web/**. La vista previa de IntelliJ en el puerto `63342` solo muestra el HTML y no conecta con la API, así que el login no funciona desde esa dirección.

También puedes iniciarla manualmente desde PowerShell:

   ```powershell
   .\iniciar_web.ps1
   ```

   El script compila el WAR, lo despliega en Tomcat y solicita el usuario y la contraseña de MySQL si no están configurados en las variables de entorno.
Al terminar, deja abierta la ventana de PowerShell y visita **http://localhost:8080/angur-web/**.

Si PowerShell bloquea el script, ejecútalo para esa ventana con:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\iniciar_web.ps1
```

CATALINA_HOME puede apuntar a otra carpeta de Tomcat. Si Maven o Java no están en PATH, instálalos o configura sus variables de entorno antes de iniciar.

## Preparar cuentas de práctica

Si la tabla usuarios ya contiene cuentas activas, inicia sesión con ellas. Para crear las cinco cuentas de práctica incluidas en SeedDemoUsers, primero compila el proyecto y configura las variables de conexión de MySQL. El usuario de base de datos necesita permiso INSERT durante este paso.

En PowerShell, define una contraseña de práctica de 12 caracteres o más y ejecuta la utilidad:

```powershell
$env:ANGUR_DB_URL = 'jdbc:mysql://127.0.0.1:3306/angur_gestion?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC'
$env:ANGUR_DB_USER = 'angur_app'
$secureDbPassword = Read-Host 'Contraseña MySQL' -AsSecureString
$env:ANGUR_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secureDbPassword).Password
$secureSeedPassword = Read-Host 'Contraseña para las cuentas de práctica (12 caracteres o más)' -AsSecureString
$env:ANGUR_SEED_PASSWORD = [System.Net.NetworkCredential]::new('', $secureSeedPassword).Password
mvn -f web/pom.xml package
$connector = Get-ChildItem .\web\target\angur-web\WEB-INF\lib\mysql-connector-j-*.jar | Select-Object -First 1
java -cp ".\web\target\classes;$($connector.FullName)" mx.velian.angur.auth.SeedDemoUsers
Remove-Item Env:ANGUR_SEED_PASSWORD
```

Los nombres que se crean son **administrador**, **vendedor**, **cliente**, **supervisor** y **almacen**. Las cuentas nuevas usan la contraseña de práctica que elegiste; las que ya existían conservan su contraseña. No se guarda esa contraseña en el repositorio.

## Abrir la aplicación Android

1. Abre Android Studio y selecciona **File > Open**.
2. Elige la carpeta **android** dentro del repositorio, espera a que termine **Gradle Sync** y acepta instalar los componentes de Android SDK que falten.
3. Inicia primero MySQL y la versión web con los pasos anteriores.
4. Selecciona la configuración **app**, elige un emulador y pulsa **Run**.

La variante debug ya apunta desde el emulador a **http://10.0.2.2:8080/angur-web**, que llega a Tomcat en el equipo. Un teléfono físico requiere cambiar esa URL por una dirección accesible desde el teléfono; para un despliegue real debe utilizarse HTTPS.

## Estructura

- **web/**: interfaz HTML/CSS/JavaScript y servicio Java.
- **android/**: aplicación Android Studio.
- **db/schema.sql**: base de datos y tabla de usuarios.
- **iniciar_web.ps1**: compilación y arranque local de la web.
- **docs/**: especificación y notas del proyecto.
