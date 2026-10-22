-- =====================================================================
-- Chispas Inclusivas - Script de creación + datos de ejemplo
-- Motor: MySQL 8.4 LTS  |  Charset: utf8mb4  |  Normalizado a 3FN
-- Ejecutar completo: mysql -u root -p < db/chispas_bd.sql
-- (con Docker se carga solo la primera vez que levanta la base, ver docker-compose.yml)
-- Contraseña de todos los usuarios de ejemplo: Chispas2026 (BCrypt)
-- Imágenes de ejemplo: picsum.photos (públicas, reemplazables)
-- =====================================================================
-- el archivo es UTF-8: sin esto, algunos clientes (como el de Docker) guardan mal los acentos
SET NAMES utf8mb4;
DROP DATABASE IF EXISTS chispas;
CREATE DATABASE chispas CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE chispas;

-- ---------------------------------------------------------------------
-- 1. USUARIOS Y PERFIL  (CHIS-1, CHIS-2)
-- ---------------------------------------------------------------------
CREATE TABLE ciclo_lectivo (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  anio          INT NOT NULL UNIQUE,          -- 2026
  fecha_inicio  DATE NOT NULL,
  fecha_fin     DATE NOT NULL,
  activo        BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT ck_ciclo_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE TABLE curso (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  ciclo_lectivo_id BIGINT UNSIGNED NOT NULL,
  nombre           VARCHAR(30) NOT NULL,           -- "4to Año"
  division         VARCHAR(10) NULL,
  CONSTRAINT uq_curso UNIQUE (ciclo_lectivo_id, nombre, division),
  CONSTRAINT fk_curso_ciclo FOREIGN KEY (ciclo_lectivo_id) REFERENCES ciclo_lectivo(id)
);

CREATE TABLE usuario (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dni            VARCHAR(12)  NOT NULL UNIQUE,
  email          VARCHAR(120) NULL UNIQUE,
  password_hash  VARCHAR(100) NOT NULL,
  nombre         VARCHAR(60)  NOT NULL,
  apellido       VARCHAR(60)  NOT NULL,
  rol            ENUM('ESTUDIANTE','DOCENTE','PRECEPTOR','ADMIN') NOT NULL,
  foto_url       VARCHAR(255) NULL,
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- TSDS-4: tabla que usa el back para "olvidé mi contraseña" (entidad PasswordResetToken).
-- Faltaba en el script; con ddl-auto=validate la app no arranca sin ella.
CREATE TABLE password_reset_tokens (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  token        VARCHAR(255) NOT NULL UNIQUE,
  usuario_id   BIGINT UNSIGNED NOT NULL UNIQUE,      -- un token vigente por usuario
  expiry_date  DATETIME(6)  NOT NULL,
  CONSTRAINT fk_prt_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE estudiante (
  usuario_id     BIGINT UNSIGNED PRIMARY KEY,
  observaciones  TEXT NULL,
  CONSTRAINT fk_est_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE capacidad (
  id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  area      VARCHAR(60)  NOT NULL,
  pregunta  VARCHAR(255) NOT NULL,
  orden     INT UNSIGNED NOT NULL DEFAULT 1
);

CREATE TABLE estudiante_capacidad (
  estudiante_id  BIGINT UNSIGNED NOT NULL,
  capacidad_id   BIGINT UNSIGNED NOT NULL,
  respuesta      BOOLEAN NOT NULL,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (estudiante_id, capacidad_id),
  CONSTRAINT fk_ec_est FOREIGN KEY (estudiante_id) REFERENCES estudiante(usuario_id) ON DELETE CASCADE,
  CONSTRAINT fk_ec_cap FOREIGN KEY (capacidad_id)  REFERENCES capacidad(id)
);

-- Perfil del estudiante: catálogo de condiciones médicas (TDA, dislexia, ...)
CREATE TABLE condicion (
  id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(60) NOT NULL UNIQUE
);

-- un estudiante puede tener 0, 1 o varias condiciones
CREATE TABLE estudiante_condicion (
  estudiante_id  BIGINT UNSIGNED NOT NULL,
  condicion_id   BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (estudiante_id, condicion_id),
  CONSTRAINT fk_econd_est  FOREIGN KEY (estudiante_id) REFERENCES estudiante(usuario_id) ON DELETE CASCADE,
  CONSTRAINT fk_econd_cond FOREIGN KEY (condicion_id)  REFERENCES condicion(id)
);

-- 2. ESPACIOS CURRICULARES, CARPETAS, HORARIOS Y CONTENIDOS  (CHIS-3, CHIS-6)
CREATE TABLE espacio_curricular (
  id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(60)  NOT NULL UNIQUE,            -- "Lengua y Literatura"
  icono   VARCHAR(255) NULL,
  color   CHAR(7)      NULL
);

-- Carpeta digital: UNA por estudiante y por ciclo lectivo.
-- Registra además el curso del estudiante ese año.
CREATE TABLE carpeta (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  estudiante_id    BIGINT UNSIGNED NOT NULL,
  ciclo_lectivo_id BIGINT UNSIGNED NOT NULL,
  curso_id         BIGINT UNSIGNED NOT NULL,
  nombre           VARCHAR(60) NOT NULL,            -- "Carpeta Rosa"
  color            CHAR(7) NULL,
  creada_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_carpeta UNIQUE (estudiante_id, ciclo_lectivo_id),
  CONSTRAINT fk_carp_est   FOREIGN KEY (estudiante_id)    REFERENCES estudiante(usuario_id) ON DELETE CASCADE,
  CONSTRAINT fk_carp_ciclo FOREIGN KEY (ciclo_lectivo_id) REFERENCES ciclo_lectivo(id),
  CONSTRAINT fk_carp_curso FOREIGN KEY (curso_id)         REFERENCES curso(id)
);

-- Secciones de la carpeta: un espacio curricular por sección
CREATE TABLE carpeta_espacio (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  carpeta_id            BIGINT UNSIGNED NOT NULL,
  espacio_curricular_id BIGINT UNSIGNED NOT NULL,
  orden                 INT UNSIGNED NOT NULL DEFAULT 1,  -- orden en "Mis Materias"
  CONSTRAINT uq_carpeta_espacio UNIQUE (carpeta_id, espacio_curricular_id),
  CONSTRAINT fk_ce_carpeta FOREIGN KEY (carpeta_id)            REFERENCES carpeta(id) ON DELETE CASCADE,
  CONSTRAINT fk_ce_espacio FOREIGN KEY (espacio_curricular_id) REFERENCES espacio_curricular(id)
);

CREATE TABLE horario (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  curso_id     BIGINT UNSIGNED NOT NULL,
  espacio_curricular_id BIGINT UNSIGNED NOT NULL,
  docente_id   BIGINT UNSIGNED NULL,
  dia          ENUM('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES') NOT NULL,
  hora_inicio  TIME NOT NULL,
  hora_fin     TIME NOT NULL,
  CONSTRAINT ck_horario_horas CHECK (hora_fin > hora_inicio),
  CONSTRAINT uq_horario UNIQUE (curso_id, dia, hora_inicio),
  CONSTRAINT fk_hor_curso   FOREIGN KEY (curso_id)   REFERENCES curso(id),
  CONSTRAINT fk_hor_espacio FOREIGN KEY (espacio_curricular_id) REFERENCES espacio_curricular(id),
  CONSTRAINT fk_hor_docente FOREIGN KEY (docente_id) REFERENCES usuario(id)
);

CREATE TABLE contenido (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  espacio_curricular_id BIGINT UNSIGNED NOT NULL,
  tema        VARCHAR(120) NOT NULL,
  texto       VARCHAR(255) NOT NULL,
  portada_url VARCHAR(255) NULL,
  audio_url   VARCHAR(255) NULL,
  CONSTRAINT fk_cont_espacio FOREIGN KEY (espacio_curricular_id) REFERENCES espacio_curricular(id)
);

-- 3. IMÁGENES (catálogo reutilizable)
--    El archivo vive fuera de la BD; aquí solo la ubicación.
--    EXTERNA -> ubicacion = URL completa
--    LOCAL   -> ubicacion = clave relativa (actividades/uuid.webp)
CREATE TABLE imagen (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  origen      ENUM('LOCAL','EXTERNA') NOT NULL,
  ubicacion   VARCHAR(255) NOT NULL UNIQUE,
  nombre      VARCHAR(80)  NULL,
  texto_alt   VARCHAR(160) NOT NULL,      -- accesibilidad (lector de pantalla)
  mime_type   VARCHAR(30)  NULL,
  tamanio_kb  INT UNSIGNED NULL,
  subida_por  BIGINT UNSIGNED NOT NULL,
  subida_en   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_img_usuario FOREIGN KEY (subida_por) REFERENCES usuario(id)
);

-- 4. ACTIVIDADES
CREATE TABLE plantilla (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo       ENUM('ORDENAR','UNIR_FLECHAS') NOT NULL UNIQUE,
  nombre       VARCHAR(60)  NOT NULL,
  destinatario VARCHAR(120) NULL,
  min_items    INT UNSIGNED NOT NULL,   -- validado en el servicio
  max_items    INT UNSIGNED NOT NULL,
  CONSTRAINT ck_plantilla_items CHECK (max_items >= min_items)
);

CREATE TABLE actividad (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  contenido_id  BIGINT UNSIGNED NOT NULL,
  plantilla_id  BIGINT UNSIGNED NOT NULL,
  creada_por    BIGINT UNSIGNED NOT NULL,
  titulo        VARCHAR(100) NOT NULL,
  consigna      VARCHAR(255) NOT NULL,
  creada_en     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_act_cont  FOREIGN KEY (contenido_id) REFERENCES contenido(id),
  CONSTRAINT fk_act_plant FOREIGN KEY (plantilla_id) REFERENCES plantilla(id),
  CONSTRAINT fk_act_doc   FOREIGN KEY (creada_por)   REFERENCES usuario(id)
);

-- Imágenes usadas por una actividad (N:M actividad-imagen)
--   ORDENAR: nro_orden = posición correcta (1..N)
--   UNIR_FLECHAS: nro_orden = posición en pantalla dentro de su columna
CREATE TABLE actividad_imagen (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  actividad_id  BIGINT UNSIGNED NOT NULL,
  imagen_id     BIGINT UNSIGNED NOT NULL,
  titulo        VARCHAR(80)  NULL,          -- rótulo visible: "Sale de su casa"
  nro_orden     INT UNSIGNED NOT NULL,
  lado          ENUM('UNICA','IZQ','DER') NOT NULL DEFAULT 'UNICA', -- IZQ/DER solo UNIR_FLECHAS
  CONSTRAINT uq_ai_imagen UNIQUE (actividad_id, imagen_id),
  CONSTRAINT uq_ai_orden  UNIQUE (actividad_id, lado, nro_orden),
  CONSTRAINT fk_ai_act FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE,
  CONSTRAINT fk_ai_img FOREIGN KEY (imagen_id)    REFERENCES imagen(id)
);

-- Solución de UNIR_FLECHAS: un par = una fila
CREATE TABLE actividad_par (
  id             BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  actividad_id   BIGINT UNSIGNED NOT NULL,
  item_izq_id    BIGINT UNSIGNED NOT NULL,
  item_der_id    BIGINT UNSIGNED NOT NULL,
  CONSTRAINT uq_par_izq UNIQUE (actividad_id, item_izq_id),
  CONSTRAINT uq_par_der UNIQUE (actividad_id, item_der_id),
  CONSTRAINT fk_par_act FOREIGN KEY (actividad_id) REFERENCES actividad(id) ON DELETE CASCADE,
  CONSTRAINT fk_par_izq FOREIGN KEY (item_izq_id)  REFERENCES actividad_imagen(id) ON DELETE CASCADE,
  CONSTRAINT fk_par_der FOREIGN KEY (item_der_id)  REFERENCES actividad_imagen(id) ON DELETE CASCADE
);

-- 5. TAREAS Y RESPUESTAS  (CHIS-4, CHIS-5)
CREATE TABLE tarea (
  id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  actividad_id       BIGINT UNSIGNED NOT NULL,
  carpeta_espacio_id BIGINT UNSIGNED NOT NULL,         -- carpeta del estudiante + espacio
  asignada_por       BIGINT UNSIGNED NOT NULL,
  fecha_asignacion   DATE NOT NULL,
  asignada_en        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- TSDS-4: hora exacta de asignación (desempata tareas del mismo día)
  estado            ENUM('PENDIENTE','EN_CURSO','FINALIZADA') NOT NULL DEFAULT 'PENDIENTE',
  iniciada_en        DATETIME NULL,
  finalizada_en      DATETIME NULL,
  duracion_minutos   INT UNSIGNED NULL,
  aciertos           INT UNSIGNED NULL,
  total_items        INT UNSIGNED NULL,
  CONSTRAINT uq_tarea UNIQUE (actividad_id, carpeta_espacio_id, fecha_asignacion),
  CONSTRAINT fk_tar_act FOREIGN KEY (actividad_id)       REFERENCES actividad(id),
  CONSTRAINT fk_tar_ce  FOREIGN KEY (carpeta_espacio_id) REFERENCES carpeta_espacio(id),
  CONSTRAINT fk_tar_doc FOREIGN KEY (asignada_por)  REFERENCES usuario(id)
);
-- TSDS-4: se suma asignada_en para ordenar por fecha y hora sin trabajo extra
CREATE INDEX ix_tarea_ce_fecha ON tarea (carpeta_espacio_id, fecha_asignacion, asignada_en);

-- Una fila por ítem respondido y por intento
--   ORDENAR: posicion_elegida
--   UNIR_FLECHAS: item_unido_id (ítem DER que eligió para el IZQ)
-- TSDS-13: cada entrega es un intento nuevo (1, 2, 3...); el historial no se borra
CREATE TABLE respuesta (
  tarea_id          BIGINT UNSIGNED NOT NULL,
  item_id           BIGINT UNSIGNED NOT NULL,
  intento           INT UNSIGNED NOT NULL DEFAULT 1,
  posicion_elegida  INT UNSIGNED NULL,
  item_unido_id     BIGINT UNSIGNED NULL,
  es_correcta       BOOLEAN NOT NULL,
  PRIMARY KEY (tarea_id, item_id, intento),
  CONSTRAINT fk_resp_tarea FOREIGN KEY (tarea_id)      REFERENCES tarea(id) ON DELETE CASCADE,
  CONSTRAINT fk_resp_item  FOREIGN KEY (item_id)       REFERENCES actividad_imagen(id),
  CONSTRAINT fk_resp_union FOREIGN KEY (item_unido_id) REFERENCES actividad_imagen(id)
);

-- INSERTS DE EJEMPLO
INSERT INTO ciclo_lectivo (id, anio, fecha_inicio, fecha_fin, activo) VALUES
 (1,2025,'2025-03-03','2025-12-12',FALSE),
 (2,2026,'2026-03-02','2026-12-11',TRUE);

INSERT INTO curso (id, ciclo_lectivo_id, nombre, division) VALUES
 (1,2,'4to Año','A'),
 (2,1,'3er Año','A');

INSERT INTO usuario (id, dni, email, password_hash, nombre, apellido, rol) VALUES
 (1,'20111222','docente@chispas.edu.ar','$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Laura','Gómez','DOCENTE'),
 (2,'45123456',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Mariel','Linera','ESTUDIANTE'),
 (3,'45654321',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Tomás','Ríos','ESTUDIANTE');

INSERT INTO estudiante (usuario_id) VALUES (2), (3);

INSERT INTO capacidad (id, area, pregunta, orden) VALUES
 (1,'Comunicación y Comprensión','¿Se comunica mediante lenguaje oral?',1),
 (2,'Lectura y Escritura','¿Se encuentra alfabetizado/a o realiza lectura convencional?',2),
 (3,'Nociones Matemáticas','¿Reconoce o escribe números de manera autónoma?',3),
 (4,'Atención','¿Mantiene periodos de atención breves que requieren actividades cortas?',4);

INSERT INTO estudiante_capacidad (estudiante_id, capacidad_id, respuesta) VALUES
 (2,1,TRUE),(2,2,FALSE),(2,3,TRUE),(2,4,TRUE);

INSERT INTO espacio_curricular (id, nombre, color) VALUES
 (1,'Lengua y Literatura','#4F7BE8'), (2,'Matemática','#E85F7B'), (3,'Historia','#C08A3E');

-- Mariel: carpeta 2025 (historial) y 2026 (actual). Tomás: carpeta 2026.
INSERT INTO carpeta (id, estudiante_id, ciclo_lectivo_id, curso_id, nombre, color) VALUES
 (1,2,1,2,'Carpeta Celeste','#7BC4E8'),
 (2,2,2,1,'Carpeta Rosa','#E87BB0'),
 (3,3,2,1,'Carpeta Verde','#7BE89A');

INSERT INTO carpeta_espacio (id, carpeta_id, espacio_curricular_id, orden) VALUES
 (1,1,1,1), (2,1,2,2),                 -- 2025: Lengua, Matemática
 (3,2,1,1), (4,2,2,2), (5,2,3,3),      -- 2026: Lengua, Matemática, Historia
 (6,3,1,1), (7,3,2,2);

-- horario semanal de 4to A: Lengua la da Laura (docente 1), las otras no tienen docente cargado
INSERT INTO horario (curso_id, espacio_curricular_id, docente_id, dia, hora_inicio, hora_fin) VALUES
 (1,1,1,'LUNES','08:45','09:30'),
 (1,2,NULL,'LUNES','09:30','10:15'),
 (1,3,NULL,'LUNES','10:15','11:00'),
 (1,2,NULL,'MARTES','08:45','09:30'),
 (1,3,NULL,'MARTES','09:30','10:15'),
 (1,1,1,'MARTES','10:15','11:00'),
 (1,3,NULL,'MIERCOLES','08:45','09:30'),
 (1,1,1,'MIERCOLES','09:30','10:15'),
 (1,2,NULL,'MIERCOLES','10:15','11:00'),
 (1,1,1,'JUEVES','08:45','09:30'),
 (1,3,NULL,'JUEVES','09:30','10:15'),
 (1,2,NULL,'JUEVES','10:15','11:00'),
 (1,2,NULL,'VIERNES','08:45','09:30'),
 (1,1,1,'VIERNES','09:30','10:15'),
 (1,3,NULL,'VIERNES','10:15','11:00');

-- Perfil del estudiante: Mariel tiene 2 condiciones, Tomás ninguna (los dos son alumnos de Laura).
-- Pablo (docente 4) da Lengua en 5to Año B, donde está Lucía (alumna 5, 1 condición): Laura no la tiene asignada.
INSERT INTO condicion (id, nombre) VALUES
 (1,'TDA'), (2,'Dislexia'), (3,'Autismo'), (4,'Síndrome de Down'), (5,'Discalculia');

INSERT INTO curso (id, ciclo_lectivo_id, nombre, division) VALUES
 (3,2,'5to Año','B');

INSERT INTO usuario (id, dni, email, password_hash, nombre, apellido, rol) VALUES
 (4,'20333444','docente2@chispas.edu.ar','$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Pablo','Suárez','DOCENTE'),
 (5,'46111222',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Lucía','Benítez','ESTUDIANTE');

INSERT INTO estudiante (usuario_id) VALUES (5);

INSERT INTO carpeta (id, estudiante_id, ciclo_lectivo_id, curso_id, nombre, color) VALUES
 (4,5,2,3,'Carpeta Amarilla','#E8D27B');

INSERT INTO carpeta_espacio (id, carpeta_id, espacio_curricular_id, orden) VALUES
 (8,4,1,1);

INSERT INTO horario (curso_id, espacio_curricular_id, docente_id, dia, hora_inicio, hora_fin) VALUES
 (3,1,4,'LUNES','08:45','09:30'),
 (3,1,4,'MARTES','08:45','09:30'),
 (3,1,4,'MIERCOLES','08:45','09:30'),
 (3,1,4,'JUEVES','08:45','09:30'),
 (3,1,4,'VIERNES','08:45','09:30');

INSERT INTO estudiante_condicion (estudiante_id, condicion_id) VALUES
 (2,1), (2,2),
 (5,3);

INSERT INTO contenido (id, espacio_curricular_id, tema, texto, portada_url) VALUES
 (1,1,'Literatura Española','Don Quijote de la Mancha','https://picsum.photos/seed/quijote-portada/400/600'),
 (2,1,'Vida cotidiana','Rutina de la mañana','https://picsum.photos/seed/rutina-portada/400/600');

INSERT INTO plantilla (id, codigo, nombre, destinatario, min_items, max_items) VALUES
 (1,'ORDENAR','Ordenar la historia','Secuencia temporal',3,8),
 (2,'UNIR_FLECHAS','Relación con imágenes','Asociación objeto-acción',2,5);  -- pares

-- Catálogo de imágenes (URLs públicas)
INSERT INTO imagen (id, origen, ubicacion, nombre, texto_alt, subida_por) VALUES
 ( 1,'EXTERNA','https://picsum.photos/seed/quijote-sale/400/300',   'quijote-sale',    'Don Quijote sale de su casa a caballo',1),
 ( 2,'EXTERNA','https://picsum.photos/seed/quijote-lee/400/300',    'quijote-lee',     'Don Quijote lee libros de caballería',1),
 ( 3,'EXTERNA','https://picsum.photos/seed/quijote-vuelve/400/300', 'quijote-vuelve',  'Don Quijote vuelve a su casa',1),
 ( 4,'EXTERNA','https://picsum.photos/seed/quijote-molinos/400/300','quijote-molinos', 'Don Quijote enfrenta a los molinos de viento',1),
 ( 5,'EXTERNA','https://picsum.photos/seed/bostezo/300/300',        'bostezo',         'Persona bostezando con sueño',1),
 ( 6,'EXTERNA','https://picsum.photos/seed/cama/300/300',           'cama',            'Cama en un dormitorio',1),
 ( 7,'EXTERNA','https://picsum.photos/seed/llave/300/300',          'llave',           'Llave antigua',1),
 ( 8,'EXTERNA','https://picsum.photos/seed/puerta/300/300',         'puerta',          'Puerta de madera',1),
 ( 9,'EXTERNA','https://picsum.photos/seed/despertador/300/300',    'despertador',     'Reloj despertador sonando',1),
 (10,'EXTERNA','https://picsum.photos/seed/cepillo/300/300',        'cepillo',         'Cepillo de dientes',1),
 (11,'EXTERNA','https://picsum.photos/seed/desayuno/300/300',       'desayuno',        'Taza de leche y tostadas',1),
 (12,'EXTERNA','https://picsum.photos/seed/mochila/300/300',        'mochila',         'Mochila escolar',1),
 (13,'EXTERNA','https://picsum.photos/seed/escuela/300/300',        'escuela',         'Edificio de una escuela',1),
 (14,'EXTERNA','https://picsum.photos/seed/dientes/300/300',        'dientes',         'Sonrisa con dientes limpios',1);

-- Actividad 1: ORDENAR con 4 imágenes (Don Quijote)
INSERT INTO actividad (id, contenido_id, plantilla_id, creada_por, titulo, consigna) VALUES
 (1,1,1,1,'Ordenar la historia','Arrastrá las imágenes en orden');

INSERT INTO actividad_imagen (id, actividad_id, imagen_id, titulo, nro_orden) VALUES
 (1,1,2,'Don Quijote lee',1),
 (2,1,1,'Sale de su casa',2),
 (3,1,4,'Vs Molinos',3),
 (4,1,3,'Vuelve a casa',4);

-- Actividad 2: ORDENAR con 5 imágenes (otra cantidad, misma estructura)

INSERT INTO actividad (id, contenido_id, plantilla_id, creada_por, titulo, consigna) VALUES
 (2,2,1,1,'Mi mañana','Ordená lo que hacés al levantarte');

INSERT INTO actividad_imagen (id, actividad_id, imagen_id, titulo, nro_orden) VALUES
 (5,2,9, 'Suena el despertador',1),
 (6,2,10,'Me lavo los dientes',2),
 (7,2,11,'Desayuno',3),
 (8,2,12,'Preparo la mochila',4),
 (9,2,13,'Voy a la escuela',5);

-- Actividad 3: UNIR_FLECHAS con 2 pares (del prototipo Figma)
INSERT INTO actividad (id, contenido_id, plantilla_id, creada_por, titulo, consigna) VALUES
 (3,1,2,1,'Unir con flechas','Uní cada imagen con la que corresponde');

INSERT INTO actividad_imagen (id, actividad_id, imagen_id, titulo, nro_orden, lado) VALUES
 (10,3,5,'Sueño',  1,'IZQ'),
 (11,3,7,'Llave',  2,'IZQ'),
 (12,3,8,'Puerta', 1,'DER'),   -- la columna derecha se muestra mezclada
 (13,3,6,'Cama',   2,'DER');

INSERT INTO actividad_par (actividad_id, item_izq_id, item_der_id) VALUES
 (3,10,13),   -- Sueño -> Cama
 (3,11,12);   -- Llave -> Puerta


-- Actividad 4: UNIR_FLECHAS con 3 pares (reutiliza imágenes 6 y 10)
INSERT INTO actividad (id, contenido_id, plantilla_id, creada_por, titulo, consigna) VALUES
 (4,2,2,1,'¿Qué uso para...?','Uní cada acción con su objeto');

INSERT INTO actividad_imagen (id, actividad_id, imagen_id, titulo, nro_orden, lado) VALUES
 (14,4,14,'Dientes limpios',1,'IZQ'),
 (15,4,5, 'Tengo sueño',    2,'IZQ'),
 (16,4,13,'Ir a estudiar',  3,'IZQ'),
 (17,4,6, 'Cama',           1,'DER'),
 (18,4,12,'Mochila',        2,'DER'),
 (19,4,10,'Cepillo',        3,'DER');

INSERT INTO actividad_par (actividad_id, item_izq_id, item_der_id) VALUES
 (4,14,19), (4,15,17), (4,16,18);

-- Tareas y respuestas
-- TSDS-4: se agrega asignada_en. Las tareas 1 y 2 de Mariel son del mismo día:
-- la 2 se asignó antes que la 1, así que el orden por hora (1, 2) difiere del orden por id
INSERT INTO tarea (id, actividad_id, carpeta_espacio_id, asignada_por, fecha_asignacion, asignada_en, estado,
                   iniciada_en, finalizada_en, duracion_minutos, aciertos, total_items) VALUES
 (1,1,3,1,'2026-10-16','2026-10-16 08:45','FINALIZADA','2026-10-16 08:50','2026-10-16 08:57',7,2,4),
 (2,3,3,1,'2026-10-16','2026-10-16 08:30','PENDIENTE',NULL,NULL,NULL,NULL,NULL),
 (3,2,6,1,'2026-10-16','2026-10-16 08:40','PENDIENTE',NULL,NULL,NULL,NULL,NULL);

-- Tarea 1 (ORDENAR): Mariel intercambió "Vs Molinos" y "Vuelve a casa"
-- -> 2 aciertos de 4
INSERT INTO respuesta (tarea_id, item_id, intento, posicion_elegida, es_correcta) VALUES
 (1,1,1,1,TRUE),
 (1,2,1,2,TRUE),
 (1,3,1,4,FALSE),
 (1,4,1,3,FALSE);

-- =====================================================================
-- MIGRACIÓN TSDS-4 (solo para quien ya tenga la BD creada sin asignada_en)
-- =====================================================================
-- ALTER TABLE tarea ADD COLUMN asignada_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER fecha_asignacion;
-- UPDATE tarea SET asignada_en = TIMESTAMP(fecha_asignacion, '08:00:00');  -- las existentes quedan a las 8 de su día
-- DROP INDEX ix_tarea_ce_fecha ON tarea;
-- CREATE INDEX ix_tarea_ce_fecha ON tarea (carpeta_espacio_id, fecha_asignacion, asignada_en);

-- =====================================================================
-- MIGRACIÓN TSDS-13 (solo para quien ya tenga la BD creada sin intento)
-- =====================================================================
-- ALTER TABLE respuesta ADD COLUMN intento INT UNSIGNED NOT NULL DEFAULT 1 AFTER item_id,
--   DROP PRIMARY KEY, ADD PRIMARY KEY (tarea_id, item_id, intento);

-- =====================================================================
-- CONSULTAS DE PRUEBA
-- =====================================================================
-- Cantidad de ítems por actividad
-- SELECT a.titulo, p.codigo, COUNT(ai.id) AS items
--   FROM actividad a JOIN plantilla p ON p.id = a.plantilla_id
--   JOIN actividad_imagen ai ON ai.actividad_id = a.id
--  GROUP BY a.id;

-- Imágenes de una actividad ORDENAR, en orden correcto
-- SELECT ai.nro_orden, ai.titulo, i.ubicacion, i.texto_alt
--   FROM actividad_imagen ai JOIN imagen i ON i.id = ai.imagen_id
--  WHERE ai.actividad_id = 1 ORDER BY ai.nro_orden;

-- Solución de una actividad UNIR_FLECHAS
-- SELECT izq.titulo AS izquierda, der.titulo AS derecha
--   FROM actividad_par ap
--   JOIN actividad_imagen izq ON izq.id = ap.item_izq_id
--   JOIN actividad_imagen der ON der.id = ap.item_der_id
--  WHERE ap.actividad_id = 3;

-- Carpeta actual de un estudiante con sus espacios (CHIS-3)
-- SELECT c.nombre AS carpeta, ec.nombre AS espacio, ce.orden
--   FROM carpeta c
--   JOIN ciclo_lectivo cl ON cl.id = c.ciclo_lectivo_id AND cl.activo
--   JOIN carpeta_espacio ce ON ce.carpeta_id = c.id
--   JOIN espacio_curricular ec ON ec.id = ce.espacio_curricular_id
--  WHERE c.estudiante_id = 2 ORDER BY ce.orden;

-- Tareas de un estudiante en un espacio, por fecha (CHIS-4)
-- SELECT ec.nombre AS espacio, a.titulo, t.fecha_asignacion, t.estado
--   FROM tarea t
--   JOIN carpeta_espacio ce ON ce.id = t.carpeta_espacio_id
--   JOIN carpeta c ON c.id = ce.carpeta_id
--   JOIN espacio_curricular ec ON ec.id = ce.espacio_curricular_id
--   JOIN actividad a ON a.id = t.actividad_id
--  WHERE c.estudiante_id = 2 AND c.ciclo_lectivo_id = 2
--    AND ec.id = 1 ORDER BY t.fecha_asignacion DESC;

-- Horarios de Lunes de Mariel
--    SELECT ec.nombre AS espacio, h.hora_inicio, h.hora_fin
--    FROM carpeta c
--    JOIN ciclo_lectivo cl      ON cl.id = c.ciclo_lectivo_id AND cl.activo
--    JOIN horario h             ON h.curso_id = c.curso_id
--    JOIN espacio_curricular ec ON ec.id = h.espacio_curricular_id
--    WHERE c.estudiante_id = 2
--      AND h.dia = 'LUNES'
--    ORDER BY h.hora_inicio;