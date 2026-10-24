-- =====================================================================
-- Chispas Inclusivas - Estructura de la base (tablas)
-- Motor: MySQL 8  |  Charset: utf8mb4  |  Normalizado a 3FN
-- La ejecuta Spring en cada arranque (spring.sql.init): CREATE TABLE IF NOT EXISTS,
-- asi crea las tablas la primera vez y despues no hace nada.
-- La base la crea el conector (createDatabaseIfNotExist=true) o Docker (MYSQL_DATABASE).
-- Los datos de ejemplo estan en chispasInserts.sql (DatosInicialesConfig).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. USUARIOS Y PERFIL  (CHIS-1, CHIS-2)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ciclo_lectivo (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  anio          INT NOT NULL UNIQUE,          -- 2026
  fecha_inicio  DATE NOT NULL,
  fecha_fin     DATE NOT NULL,
  activo        BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT ck_ciclo_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE TABLE IF NOT EXISTS curso (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  ciclo_lectivo_id BIGINT UNSIGNED NOT NULL,
  nombre           VARCHAR(30) NOT NULL,           -- "4to Año"
  division         VARCHAR(10) NULL,
  CONSTRAINT uq_curso UNIQUE (ciclo_lectivo_id, nombre, division),
  CONSTRAINT fk_curso_ciclo FOREIGN KEY (ciclo_lectivo_id) REFERENCES ciclo_lectivo(id)
);

CREATE TABLE IF NOT EXISTS usuario (
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
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  token        VARCHAR(255) NOT NULL UNIQUE,
  usuario_id   BIGINT UNSIGNED NOT NULL UNIQUE,      -- un token vigente por usuario
  expiry_date  DATETIME(6)  NOT NULL,
  CONSTRAINT fk_prt_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS estudiante (
  usuario_id     BIGINT UNSIGNED PRIMARY KEY,
  observaciones  TEXT NULL,
  CONSTRAINT fk_est_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS capacidad (
  id        BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  area      VARCHAR(60)  NOT NULL,
  pregunta  VARCHAR(255) NOT NULL,
  orden     INT UNSIGNED NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS estudiante_capacidad (
  estudiante_id  BIGINT UNSIGNED NOT NULL,
  capacidad_id   BIGINT UNSIGNED NOT NULL,
  respuesta      BOOLEAN NOT NULL,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (estudiante_id, capacidad_id),
  CONSTRAINT fk_ec_est FOREIGN KEY (estudiante_id) REFERENCES estudiante(usuario_id) ON DELETE CASCADE,
  CONSTRAINT fk_ec_cap FOREIGN KEY (capacidad_id)  REFERENCES capacidad(id)
);

-- Perfil del estudiante: catálogo de condiciones médicas (TDA, dislexia, ...)
CREATE TABLE IF NOT EXISTS condicion (
  id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(60) NOT NULL UNIQUE
);

-- un estudiante puede tener 0, 1 o varias condiciones
CREATE TABLE IF NOT EXISTS estudiante_condicion (
  estudiante_id  BIGINT UNSIGNED NOT NULL,
  condicion_id   BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (estudiante_id, condicion_id),
  CONSTRAINT fk_econd_est  FOREIGN KEY (estudiante_id) REFERENCES estudiante(usuario_id) ON DELETE CASCADE,
  CONSTRAINT fk_econd_cond FOREIGN KEY (condicion_id)  REFERENCES condicion(id)
);

-- 2. ESPACIOS CURRICULARES, CARPETAS, HORARIOS Y CONTENIDOS  (CHIS-3, CHIS-6)
CREATE TABLE IF NOT EXISTS espacio_curricular (
  id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  nombre  VARCHAR(60)  NOT NULL UNIQUE,            -- "Lengua y Literatura"
  icono   VARCHAR(255) NULL,
  color   CHAR(7)      NULL
);

-- Carpeta digital: UNA por estudiante y por ciclo lectivo.
-- Registra además el curso del estudiante ese año.
CREATE TABLE IF NOT EXISTS carpeta (
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
CREATE TABLE IF NOT EXISTS carpeta_espacio (
  id                    BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  carpeta_id            BIGINT UNSIGNED NOT NULL,
  espacio_curricular_id BIGINT UNSIGNED NOT NULL,
  orden                 INT UNSIGNED NOT NULL DEFAULT 1,  -- orden en "Mis Materias"
  CONSTRAINT uq_carpeta_espacio UNIQUE (carpeta_id, espacio_curricular_id),
  CONSTRAINT fk_ce_carpeta FOREIGN KEY (carpeta_id)            REFERENCES carpeta(id) ON DELETE CASCADE,
  CONSTRAINT fk_ce_espacio FOREIGN KEY (espacio_curricular_id) REFERENCES espacio_curricular(id)
);

CREATE TABLE IF NOT EXISTS horario (
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

CREATE TABLE IF NOT EXISTS contenido (
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
CREATE TABLE IF NOT EXISTS imagen (
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
CREATE TABLE IF NOT EXISTS plantilla (
  id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  codigo       ENUM('ORDENAR','UNIR_FLECHAS') NOT NULL UNIQUE,
  nombre       VARCHAR(60)  NOT NULL,
  destinatario VARCHAR(120) NULL,
  min_items    INT UNSIGNED NOT NULL,   -- validado en el servicio
  max_items    INT UNSIGNED NOT NULL,
  CONSTRAINT ck_plantilla_items CHECK (max_items >= min_items)
);

CREATE TABLE IF NOT EXISTS actividad (
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
CREATE TABLE IF NOT EXISTS actividad_imagen (
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
CREATE TABLE IF NOT EXISTS actividad_par (
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
CREATE TABLE IF NOT EXISTS tarea (
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
  CONSTRAINT fk_tar_doc FOREIGN KEY (asignada_por)  REFERENCES usuario(id),
  -- TSDS-4: se suma asignada_en para ordenar por fecha y hora sin trabajo extra
  INDEX ix_tarea_ce_fecha (carpeta_espacio_id, fecha_asignacion, asignada_en)
);

-- Una fila por ítem respondido y por intento
--   ORDENAR: posicion_elegida
--   UNIR_FLECHAS: item_unido_id (ítem DER que eligió para el IZQ)
-- TSDS-13: cada entrega es un intento nuevo (1, 2, 3...); el historial no se borra
CREATE TABLE IF NOT EXISTS respuesta (
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
