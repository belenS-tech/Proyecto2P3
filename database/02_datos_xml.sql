-- Importación inicial: ejecutar una sola vez, después de 01_schema.sql.
-- Generado desde src/main/resources/data; los XML se conservan.
WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK
SET DEFINE OFF
INSERT INTO p3_usuarios (id,clave,rol) VALUES ('admin','admin','ADMINISTRADOR');
INSERT INTO p3_usuarios (id,clave,rol) VALUES ('123','123','FUNCIONARIO');
INSERT INTO p3_usuarios (id,clave,rol) VALUES ('222','222','FUNCIONARIO');
INSERT INTO p3_usuarios (id,clave,rol) VALUES ('111','123','FUNCIONARIO');
INSERT INTO p3_funcionarios (id,nombre,telefono) VALUES ('111','belen','123');
INSERT INTO p3_funcionarios (id,nombre,telefono) VALUES ('123','juan','222');
INSERT INTO p3_funcionarios (id,nombre,telefono) VALUES ('222','fio','12345');
INSERT INTO p3_categorias (id,descripcion) VALUES ('CAT-000001','Laptop Windows');
INSERT INTO p3_categorias (id,descripcion) VALUES ('CAT-000002','Sala de juntas');
INSERT INTO p3_categorias (id,descripcion) VALUES ('CAT-000003','Sala para 10 personas');
INSERT INTO p3_categorias (id,descripcion) VALUES ('CAT-000004','laptoppp');
INSERT INTO p3_categorias (id,descripcion) VALUES ('CAT-000005','sala de eventos');
INSERT INTO p3_recursos (id,categoria_id,descripcion) VALUES ('111','CAT-000002','para reuniones');
INSERT INTO p3_recursos (id,categoria_id,descripcion) VALUES ('12','CAT-000003','sala para charlar');
INSERT INTO p3_recursos (id,categoria_id,descripcion) VALUES ('2323','CAT-000001','laptops para uso comun');
INSERT INTO p3_recursos (id,categoria_id,descripcion) VALUES ('3434','CAT-000001','laptop123');
INSERT INTO p3_reservas (id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado) VALUES ('12','charla',TO_DATE('2026-12-09','YYYY-MM-DD'),'12:00:00','13:00:00','admin','ACTIVA');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('12','12');
INSERT INTO p3_reservas (id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado) VALUES ('11','reunion',TO_DATE('2026-09-06','YYYY-MM-DD'),'12:00:00','13:00:00','111','ACTIVA');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('11','111');
INSERT INTO p3_reservas (id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado) VALUES ('RES-MIG-000003','reunion',TO_DATE('2026-09-07','YYYY-MM-DD'),'12:00:00','13:00:00','111','ACTIVA');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('RES-MIG-000003','111');
INSERT INTO p3_reservas (id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado) VALUES ('RES-MIG-000004','2ee',TO_DATE('2026-09-07','YYYY-MM-DD'),'10:00:00','11:00:00','111','ACTIVA');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('RES-MIG-000004','111');
INSERT INTO p3_reservas (id,actividad,fecha,hora_inicio,hora_fin,funcionario_id,estado) VALUES ('RES-000007','Reserva de sala de juntas con laptop Windows',TO_DATE('2027-08-30','YYYY-MM-DD'),'13:00:00','15:00:00','111','ACTIVA');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('RES-000007','2323');
INSERT INTO p3_detalles (reserva_id,recurso_id) VALUES ('RES-000007','111');
COMMIT;
