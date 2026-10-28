-- Oracle XE 11g o superior. Ejecutar como script (F5) en SQL Developer.
-- No elimina ni modifica tablas existentes. Prefijo P3_ para este proyecto.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
CREATE TABLE p3_usuarios (
    id VARCHAR2(100 CHAR) PRIMARY KEY,
    clave VARCHAR2(255 CHAR) NOT NULL,
    rol VARCHAR2(20 CHAR) NOT NULL CHECK (rol IN ('ADMINISTRADOR','FUNCIONARIO'))
);
CREATE TABLE p3_funcionarios (
    id VARCHAR2(100 CHAR) PRIMARY KEY,
    nombre VARCHAR2(200 CHAR) NOT NULL,
    telefono VARCHAR2(50 CHAR) NOT NULL
);
CREATE TABLE p3_categorias (
    id VARCHAR2(100 CHAR) PRIMARY KEY,
    descripcion VARCHAR2(500 CHAR) NOT NULL
);
CREATE TABLE p3_recursos (
    id VARCHAR2(100 CHAR) PRIMARY KEY,
    categoria_id VARCHAR2(100 CHAR) NOT NULL REFERENCES p3_categorias(id),
    descripcion VARCHAR2(1000 CHAR) NOT NULL
);
CREATE TABLE p3_reservas (
    id VARCHAR2(100 CHAR) PRIMARY KEY,
    actividad VARCHAR2(2000 CHAR) NOT NULL,
    fecha DATE NOT NULL CHECK (fecha=TRUNC(fecha)),
    hora_inicio VARCHAR2(8 CHAR) NOT NULL,
    hora_fin VARCHAR2(8 CHAR) NOT NULL,
    -- El administrador también reserva; por eso se referencia a usuarios.
    funcionario_id VARCHAR2(100 CHAR) NOT NULL REFERENCES p3_usuarios(id),
    estado VARCHAR2(15 CHAR) NOT NULL CHECK (estado IN ('ACTIVA','CANCELADA','FINALIZADA')),
    CONSTRAINT p3_reservas_horas CHECK (hora_inicio < hora_fin),
    CONSTRAINT p3_hora_inicio_formato CHECK (REGEXP_LIKE(hora_inicio,'^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$')),
    CONSTRAINT p3_hora_fin_formato CHECK (REGEXP_LIKE(hora_fin,'^([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$'))
);
CREATE TABLE p3_detalles (
    reserva_id VARCHAR2(100 CHAR) NOT NULL REFERENCES p3_reservas(id) ON DELETE CASCADE,
    recurso_id VARCHAR2(100 CHAR) NOT NULL REFERENCES p3_recursos(id),
    PRIMARY KEY (reserva_id,recurso_id)
);
CREATE INDEX p3_recursos_categoria ON p3_recursos(categoria_id);
CREATE INDEX p3_reservas_usuario ON p3_reservas(funcionario_id);
CREATE INDEX p3_reservas_fecha ON p3_reservas(fecha,estado);
CREATE INDEX p3_detalles_recurso ON p3_detalles(recurso_id);
