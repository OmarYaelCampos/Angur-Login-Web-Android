CREATE DATABASE IF NOT EXISTS angur_gestion
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE angur_gestion;

-- Esta primera etapa solo necesita las cuentas para iniciar sesión.
CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  usuario VARCHAR(80) NOT NULL,
  correo VARCHAR(254) NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  clave_hash VARCHAR(255) NOT NULL,
  rol ENUM(
    'ADMINISTRADOR',
    'VENDEDOR',
    'CLIENTE',
    'SUPERVISOR_VENTAS',
    'ENCARGADO_ALMACEN'
  ) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_usuarios_usuario (usuario),
  UNIQUE KEY uq_usuarios_correo (correo)
) ENGINE = InnoDB;
