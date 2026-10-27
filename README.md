# Angur Gestión: login web y Android

Proyecto de inicio de sesión para cinco perfiles: administrador, vendedor, cliente, supervisor de ventas y encargado de almacén. La interfaz web y la aplicación Android usan la misma API Java, desplegada en Tomcat y conectada a MySQL.

## Código

- `web/`: interfaz HTML/CSS/JavaScript y API Java.
- `android/`: aplicación Android Studio en Java/XML.
- `db/schema.sql`: esquema MySQL.
- `docs/`: especificación, pruebas y diagramas del login.

## Requisitos

- Java 17 o posterior, Maven, MySQL 8 y Tomcat 10.1.
- Para Android: Android Studio compatible con Android Gradle Plugin 9.0.1 y SDK 36.

## Base de datos y API web

Ejecuta `db/schema.sql` en MySQL. Configura `ANGUR_DB_URL`, `ANGUR_DB_USER` y `ANGUR_DB_PASSWORD` en el entorno donde inicia Tomcat; la aplicación no guarda las credenciales en el código.

Compila la aplicación web:

```powershell
mvn -f web/pom.xml package
```

Despliega `web/target/angur-web.war` en Tomcat 10.1 e inicia Tomcat desde el entorno que contiene las variables de base de datos. La web queda disponible en `http://localhost:8080/angur-web/` y el endpoint de autenticación es `POST /angur-web/api/auth/login`.

Para cargar cuentas de práctica, configura `ANGUR_SEED_PASSWORD` con una contraseña de práctica y ejecuta la utilidad `mx.velian.angur.auth.SeedDemoUsers` con las clases de `web/target/classes` y el conector JDBC de MySQL en el classpath. La utilidad crea cinco perfiles de prueba y no modifica cuentas que ya existan.

## Aplicación Android

Abre la carpeta `android/` en Android Studio, sincroniza Gradle y ejecuta la app en un emulador. La variante `debug` se conecta a `http://10.0.2.2:8080/angur-web`, así que Tomcat debe estar activo en el equipo anfitrión. Para usar un teléfono físico o publicar una versión, configura una URL HTTPS accesible desde ese dispositivo.

## Documentación

- [Especificación del login](docs/01_especificacion_login.md)
- [Pruebas del login](docs/02_pruebas_login.md)
- [Propuesta de arquitectura](docs/03_propuesta_arquitectura_repositorios.md)
