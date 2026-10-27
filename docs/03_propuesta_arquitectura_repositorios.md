# Propuesta de desarrollo de Angur Gestión

**Fecha:** 20 de octubre de 2026  
**Base:** Documento de la empresa, requerimientos de Pruebas de Software, código local de Angur y repositorio privado `ejemplo-proyecto`.

## Repositorios identificados

| Repositorio | Función | Estado observado |
| --- | --- | --- |
| [Angur-Gestion](https://github.com/OmarYaelCampos/Angur-Gestion) | Destino del proyecto | Rama `main`; por ahora solo contiene `README.md`. El código local de Angur todavía no está allí. |
| [ejemplo-proyecto](https://github.com/OmarYaelCampos/ejemplo-proyecto) | Referencia de arquitectura y modelado | Proyecto escolar Java 17, Maven WAR, Servlet/JSP, Hibernate y MySQL. |

## Arquitectura que utiliza el ejemplo

El flujo observado es **vista JSP → Servlet → interfaz de servicio / implementación → interfaz DAO / implementación Hibernate → entidad JPA → MySQL**. Es una arquitectura por capas, cercana a MVC en la interfaz web. No hay evidencia en el repositorio de una metodología formal de gestión como Scrum.

- Las entidades [`Profesor`](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/model/Profesor.java), `Alumno` y `Materia` se corresponden con tablas. [`ProfesorMateria`](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/model/ProfesorMateria.java) modela una tabla de asociación con dos relaciones `@ManyToOne`.
- Los [DAO](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/dao/ProfesorMateriaDao.java) exponen CRUD y las [implementaciones](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/dao/impl/ProfesorMateriaDaoImpl.java) usan sesiones y transacciones Hibernate.
- Los [servicios](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/service/impl/ProfesorMateriaServiceImpl.java) median entre controladores y DAO; en el módulo consultado solo delegan, sin reglas adicionales.
- Los [Servlet](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/src/main/java/org/gerdoc/servlet/ProfesorMateriaServlet.java) reciben peticiones y redirigen a JSP. El [esquema SQL](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/sql/Escuela.sql) declara tablas base, tablas de asociación y claves foráneas.
- El [`pom.xml`](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/pom.xml) usa `javax.servlet` 4, Hibernate 5.6 y empaquetado WAR, dirigido a Tomcat 9. El proyecto de ejemplo no contiene Android.

## Decisiones para Angur

**Mantener la plataforma existente.** El Angur local ya tiene HTML/CSS/JavaScript, Java 17, Maven WAR, `jakarta.servlet` 6 para Tomcat 10.1, MySQL con JDBC y una app Android Studio Java/XML. El login de cinco roles ya existe. La tabla `usuarios` es el único esquema implementado; los diagramas P del atlas son propuestas pendientes de implementación.

**Aplicar las capas del ejemplo sin copiar sus versiones.** Organizar el backend por `model`, `dao`, `service`, `servlet`, `dto` y `util`. Mantener la vista en HTML/CSS/JavaScript conforme al diseño de Angur; los Servlet expondrán respuestas JSON para Web y Android. Para los nuevos modelos usar Jakarta Persistence y una versión de Hibernate compatible con ese espacio de nombres. Migrar el acceso de autenticación desde JDBC a un repositorio JPA de forma controlada, conservando su contrato y la tabla `usuarios`, y retirar la implementación JDBC cuando el cambio esté comprobado.

**Corregir debilidades del ejemplo desde el inicio.** Abrir y cerrar la sesión de persistencia por operación, hacer rollback solo si la transacción existe, validar entradas en Servlet y servicio, evitar SQL destructivo en migraciones, configurar credenciales con variables de entorno, aplicar autorización en el servidor y no incluir contraseñas ni secretos en Git. El [`ANALISIS.md`](https://github.com/OmarYaelCampos/ejemplo-proyecto/blob/main/ANALISIS.md) del propio ejemplo enumera varias de estas limitaciones; su estructura es la referencia, no cada detalle de implementación.

## Modelos y datos propuestos

Partir de `Usuario` (tabla actual). Añadir `Cliente`, `Producto`, `Existencia`, `MovimientoInventario`, `Cotizacion`, `PartidaCotizacion`, `Venta` y `PartidaVenta`, de acuerdo con los requerimientos y el [modelo ER propuesto](../entregables/diagramas/INDICE.md). Las partidas se modelarán como entidades de asociación con atributos propios —cantidad, precio y subtotal/costo—, de manera análoga a `ProfesorMateria` pero con reglas de negocio explícitas. Las relaciones y estados definitivos de cotización deberán validarse contra el documento principal antes de programarse.

Los scripts SQL serán incrementales y versionados: claves primarias y foráneas, índices de consulta, cantidades enteras no negativas y `DECIMAL` para dinero. Una venta registrará su encabezado, partidas, movimiento y descuento de existencia en una sola transacción; conservará el precio y costo históricos de cada partida. No se usará `DROP DATABASE` para actualizar un entorno que contenga datos.

## Distribución del código

```text
Angur/
  web/
    src/main/java/mx/velian/angur/
      model/       entidades del dominio
      dao/         contratos y persistencia
      service/     reglas de negocio y transacciones
      servlet/     API HTTP y autorización
      dto/         peticiones y respuestas JSON
      util/        configuración e infraestructura
    src/main/webapp/  HTML, CSS y JavaScript
  db/               esquema inicial, migraciones y datos de práctica
  android/
    app/src/main/java/mx/velian/angurgestion/
      ui/            pantallas y estados de presentación
      data/          cliente HTTP y repositorios
      model/         modelos de transporte de la API
  docs/             requisitos, contratos y diagramas
```

Web y Android llamarán a la **misma API** de Tomcat. Solo el servidor accederá a MySQL y decidirá permisos por rol: administrador, vendedor, cliente, supervisor de ventas y encargado de almacén. La app Android seguirá en Java/XML y compatible con Android Studio 2025.2.3.9. Al agregar pantallas protegidas, se definirá persistencia e invalidación de sesión móvil y transporte HTTPS; el login actual de Android solo muestra el resultado y no conserva sesión.

## Orden de trabajo propuesto

1. Registrar en `Angur-Gestion` el proyecto local, instrucciones de despliegue, esquema vigente y documentación; evitar binarios generados y credenciales. Esta es la primera publicación futura, no realizada en este análisis.
2. Consolidar la estructura por capas alrededor del login existente y acordar el contrato de API compartido, con validación y autorización de los cinco perfiles.
3. Desarrollar `Producto`, `Existencia` y `MovimientoInventario`, con migración SQL y pantallas Web/Android según cada perfil.
4. Desarrollar `Cliente` y `Cotizacion` con partidas y reglas de estado confirmadas con el documento principal.
5. Desarrollar `Venta` y `PartidaVenta` con transacción de inventario, historial y resultados.
6. En cada módulo mantener consistentes requisitos, modelo de clases/ER, casos de uso, API, código y casos de prueba de Pruebas de Software. Documentar cada incremento antes de integrarlo en `main`.

**Estado de esta propuesta:** análisis y diseño. No se modificó el código de aplicación, la base MySQL ni los repositorios de GitHub.
