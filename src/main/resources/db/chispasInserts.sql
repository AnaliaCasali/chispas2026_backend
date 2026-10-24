-- =====================================================================
-- Chispas Inclusivas - Datos de ejemplo
-- Los carga DatosInicialesConfig la primera vez que arranca la app (si no hay usuarios).
-- Contraseña de todos los usuarios de ejemplo: Chispas2026 (BCrypt)
-- Imágenes de ejemplo: picsum.photos (públicas, reemplazables)
-- =====================================================================
-- el archivo es UTF-8: sin esto, algunos clientes (como el de Docker) guardan mal los acentos
SET NAMES utf8mb4;


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

-- Catálogo de imágenes (URLs públicas): 4 imágenes del Quijote son Locales
INSERT INTO imagen (id, origen, ubicacion, nombre, texto_alt, subida_por) VALUES
 ( 1,'LOCAL','/images/don-quijote-saliendo.webp',  'quijote-sale',    'Don Quijote sale de su casa a caballo',1),
 ( 2,'LOCAL','/images/don-quijote-leyendo.webp',   'quijote-lee',     'Don Quijote lee libros de caballería',1),
 ( 3,'LOCAL','/images/don-quijote-volviendo.webp', 'quijote-vuelve',  'Don Quijote vuelve a su casa',1),
 ( 4,'LOCAL','/images/don-quijote-molino.webp',    'quijote-molinos', 'Don Quijote enfrenta a los molinos de viento',1),
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
