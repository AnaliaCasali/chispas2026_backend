-- Datos iniciales de Chispas: solo INSERTs (las tablas las crea Hibernate).
-- Lo ejecuta DatosInicialesConfig la primera vez que arranca la app (tabla usuario vacia).

INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (1,1,1,1,'Ordenar la historia','Arrastrá las imágenes en orden','2026-10-24 21:56:16');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (2,2,1,1,'Mi mañana','Ordená lo que hacés al levantarte','2026-10-24 21:56:16');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (3,1,2,1,'Unir con flechas','Uní cada imagen con la frase que corresponde','2026-10-24 21:56:16');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (4,2,2,1,'¿Qué uso para...?','Uní cada acción con su objeto','2026-10-24 21:56:16');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (5,1,2,1,'Causa y consecuencia','Uní cada escena con lo que pasó después','2026-10-24 23:00:23');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (1,1,2,'Don Quijote lee',1,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (2,1,1,'Sale de su casa',2,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (3,1,4,'Vs Molinos',3,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (4,1,3,'Vuelve a casa',4,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (20,5,2,'Don Quijote lee',1,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (21,5,4,'Vs Molinos',2,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (22,5,3,'Vuelve a casa',1,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (23,5,1,'Sale de su casa',2,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (24,3,2,'Don Quijote lee',1,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (25,3,1,'Sale de su casa',2,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (26,3,4,'Vs Molinos',3,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (27,3,3,'Vuelve a casa',4,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (28,3,4,'Pelea contra los molinos de viento',1,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (29,3,2,'Lee libros de caballería',2,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (30,3,3,'Vuelve a su casa',3,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (31,3,1,'Sale a caballo',4,'DER');
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (6,5,20,23);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (7,5,21,22);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (8,3,24,29);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (9,3,25,31);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (10,3,26,28);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (11,3,27,30);
INSERT INTO `capacidad` (`id`, `area`, `pregunta`, `orden`) VALUES (1,'Comunicación y Comprensión','¿Se comunica mediante lenguaje oral?',1);
INSERT INTO `capacidad` (`id`, `area`, `pregunta`, `orden`) VALUES (2,'Lectura y Escritura','¿Se encuentra alfabetizado/a o realiza lectura convencional?',2);
INSERT INTO `capacidad` (`id`, `area`, `pregunta`, `orden`) VALUES (3,'Nociones Matemáticas','¿Reconoce o escribe números de manera autónoma?',3);
INSERT INTO `capacidad` (`id`, `area`, `pregunta`, `orden`) VALUES (4,'Atención','¿Mantiene periodos de atención breves que requieren actividades cortas?',4);
INSERT INTO `carpeta` (`id`, `estudiante_id`, `ciclo_lectivo_id`, `curso_id`, `nombre`, `color`, `creada_en`) VALUES (1,2,1,2,'Carpeta Celeste','#7BC4E8','2026-10-24 21:56:16');
INSERT INTO `carpeta` (`id`, `estudiante_id`, `ciclo_lectivo_id`, `curso_id`, `nombre`, `color`, `creada_en`) VALUES (2,2,2,1,'Carpeta Rosa','#E87BB0','2026-10-24 21:56:16');
INSERT INTO `carpeta` (`id`, `estudiante_id`, `ciclo_lectivo_id`, `curso_id`, `nombre`, `color`, `creada_en`) VALUES (3,3,2,1,'Carpeta Verde','#7BE89A','2026-10-24 21:56:16');
INSERT INTO `carpeta` (`id`, `estudiante_id`, `ciclo_lectivo_id`, `curso_id`, `nombre`, `color`, `creada_en`) VALUES (4,5,2,3,'Carpeta Amarilla','#E8D27B','2026-10-24 21:56:16');
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (1,1,1,1);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (2,1,2,2);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (3,2,1,1);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (4,2,2,2);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (5,2,3,3);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (6,3,1,1);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (7,3,2,2);
INSERT INTO `carpeta_espacio` (`id`, `carpeta_id`, `espacio_curricular_id`, `orden`) VALUES (8,4,1,1);
INSERT INTO `ciclo_lectivo` (`id`, `anio`, `fecha_inicio`, `fecha_fin`, `activo`) VALUES (1,2025,'2025-03-03','2025-12-12',0);
INSERT INTO `ciclo_lectivo` (`id`, `anio`, `fecha_inicio`, `fecha_fin`, `activo`) VALUES (2,2026,'2026-03-02','2026-12-11',1);
INSERT INTO `condicion` (`id`, `nombre`) VALUES (3,'Autismo');
INSERT INTO `condicion` (`id`, `nombre`) VALUES (5,'Discalculia');
INSERT INTO `condicion` (`id`, `nombre`) VALUES (2,'Dislexia');
INSERT INTO `condicion` (`id`, `nombre`) VALUES (4,'Síndrome de Down');
INSERT INTO `condicion` (`id`, `nombre`) VALUES (1,'TDA');
INSERT INTO `contenido` (`id`, `espacio_curricular_id`, `tema`, `texto`, `portada_url`, `audio_url`) VALUES (1,1,'Literatura Española','Don Quijote de la Mancha','https://picsum.photos/seed/quijote-portada/400/600',NULL);
INSERT INTO `contenido` (`id`, `espacio_curricular_id`, `tema`, `texto`, `portada_url`, `audio_url`) VALUES (2,1,'Vida cotidiana','Rutina de la mañana','https://picsum.photos/seed/rutina-portada/400/600',NULL);
INSERT INTO `curso` (`id`, `ciclo_lectivo_id`, `nombre`, `division`) VALUES (2,1,'3er Año','A');
INSERT INTO `curso` (`id`, `ciclo_lectivo_id`, `nombre`, `division`) VALUES (1,2,'4to Año','A');
INSERT INTO `curso` (`id`, `ciclo_lectivo_id`, `nombre`, `division`) VALUES (3,2,'5to Año','B');
INSERT INTO `espacio_curricular` (`id`, `nombre`, `icono`, `color`) VALUES (1,'Lengua y Literatura',NULL,'#4F7BE8');
INSERT INTO `espacio_curricular` (`id`, `nombre`, `icono`, `color`) VALUES (2,'Matemática',NULL,'#E85F7B');
INSERT INTO `espacio_curricular` (`id`, `nombre`, `icono`, `color`) VALUES (3,'Historia',NULL,'#C08A3E');
INSERT INTO `estudiante` (`usuario_id`, `observaciones`) VALUES (2,NULL);
INSERT INTO `estudiante` (`usuario_id`, `observaciones`) VALUES (3,NULL);
INSERT INTO `estudiante` (`usuario_id`, `observaciones`) VALUES (5,NULL);
INSERT INTO `estudiante_capacidad` (`estudiante_id`, `capacidad_id`, `respuesta`, `actualizado_en`) VALUES (2,1,1,'2026-10-24 21:56:16');
INSERT INTO `estudiante_capacidad` (`estudiante_id`, `capacidad_id`, `respuesta`, `actualizado_en`) VALUES (2,2,0,'2026-10-24 21:56:16');
INSERT INTO `estudiante_capacidad` (`estudiante_id`, `capacidad_id`, `respuesta`, `actualizado_en`) VALUES (2,3,1,'2026-10-24 21:56:16');
INSERT INTO `estudiante_capacidad` (`estudiante_id`, `capacidad_id`, `respuesta`, `actualizado_en`) VALUES (2,4,1,'2026-10-24 21:56:16');
INSERT INTO `estudiante_condicion` (`estudiante_id`, `condicion_id`) VALUES (2,1);
INSERT INTO `estudiante_condicion` (`estudiante_id`, `condicion_id`) VALUES (2,2);
INSERT INTO `estudiante_condicion` (`estudiante_id`, `condicion_id`) VALUES (5,3);
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (1,1,1,1,'LUNES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (2,1,2,NULL,'LUNES','09:30:00','10:15:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (3,1,3,NULL,'LUNES','10:15:00','11:00:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (4,1,2,NULL,'MARTES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (5,1,3,NULL,'MARTES','09:30:00','10:15:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (6,1,1,1,'MARTES','10:15:00','11:00:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (7,1,3,NULL,'MIERCOLES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (8,1,1,1,'MIERCOLES','09:30:00','10:15:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (9,1,2,NULL,'MIERCOLES','10:15:00','11:00:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (10,1,1,1,'JUEVES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (11,1,3,NULL,'JUEVES','09:30:00','10:15:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (12,1,2,NULL,'JUEVES','10:15:00','11:00:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (13,1,2,NULL,'VIERNES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (14,1,1,1,'VIERNES','09:30:00','10:15:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (15,1,3,NULL,'VIERNES','10:15:00','11:00:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (16,3,1,4,'LUNES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (17,3,1,4,'MARTES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (18,3,1,4,'MIERCOLES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (19,3,1,4,'JUEVES','08:45:00','09:30:00');
INSERT INTO `horario` (`id`, `curso_id`, `espacio_curricular_id`, `docente_id`, `dia`, `hora_inicio`, `hora_fin`) VALUES (20,3,1,4,'VIERNES','08:45:00','09:30:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (1,'LOCAL','/images/don-quijote-saliendo.webp','quijote-sale','Don Quijote sale de su casa a caballo',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (2,'LOCAL','/images/don-quijote-leyendo.webp','quijote-lee','Don Quijote lee libros de caballería',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (3,'LOCAL','/images/don-quijote-volviendo.webp','quijote-vuelve','Don Quijote vuelve a su casa',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (4,'LOCAL','/images/don-quijote-molino.webp','quijote-molinos','Don Quijote enfrenta a los molinos de viento',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (5,'EXTERNA','https://picsum.photos/seed/bostezo/300/300','bostezo','Persona bostezando con sueño',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (6,'EXTERNA','https://picsum.photos/seed/cama/300/300','cama','Cama en un dormitorio',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (7,'EXTERNA','https://picsum.photos/seed/llave/300/300','llave','Llave antigua',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (8,'EXTERNA','https://picsum.photos/seed/puerta/300/300','puerta','Puerta de madera',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (9,'EXTERNA','https://picsum.photos/seed/despertador/300/300','despertador','Reloj despertador sonando',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (10,'EXTERNA','https://picsum.photos/seed/cepillo/300/300','cepillo','Cepillo de dientes',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (11,'EXTERNA','https://picsum.photos/seed/desayuno/300/300','desayuno','Taza de leche y tostadas',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (12,'EXTERNA','https://picsum.photos/seed/mochila/300/300','mochila','Mochila escolar',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (13,'EXTERNA','https://picsum.photos/seed/escuela/300/300','escuela','Edificio de una escuela',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (14,'EXTERNA','https://picsum.photos/seed/dientes/300/300','dientes','Sonrisa con dientes limpios',NULL,NULL,1,'2026-10-24 21:56:16');
INSERT INTO `plantilla` (`id`, `codigo`, `nombre`, `destinatario`, `min_items`, `max_items`) VALUES (1,'ORDENAR','Ordenar la historia','Secuencia temporal',3,8);
INSERT INTO `plantilla` (`id`, `codigo`, `nombre`, `destinatario`, `min_items`, `max_items`) VALUES (2,'UNIR_FLECHAS','Relación con imágenes','Asociación objeto-acción',2,5);
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (1,1,3,1,'2026-10-16','2026-10-16 08:45:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (5,3,3,1,'2026-10-24','2026-10-24 23:16:50','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
INSERT INTO `usuario` (`id`, `dni`, `email`, `password_hash`, `nombre`, `apellido`, `rol`, `foto_url`, `activo`, `creado_en`) VALUES (1,'20111222','docente@chispas.edu.ar','$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Laura','Gómez','DOCENTE',NULL,1,'2026-10-24 21:56:16');
INSERT INTO `usuario` (`id`, `dni`, `email`, `password_hash`, `nombre`, `apellido`, `rol`, `foto_url`, `activo`, `creado_en`) VALUES (2,'45123456',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Mariel','Linera','ESTUDIANTE','https://i.ibb.co/21dkkQLq/IMG-20261019-WA0013.jpg',1,'2026-10-24 21:56:16');
INSERT INTO `usuario` (`id`, `dni`, `email`, `password_hash`, `nombre`, `apellido`, `rol`, `foto_url`, `activo`, `creado_en`) VALUES (3,'45654321',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Tomás','Ríos','ESTUDIANTE',NULL,1,'2026-10-24 21:56:16');
INSERT INTO `usuario` (`id`, `dni`, `email`, `password_hash`, `nombre`, `apellido`, `rol`, `foto_url`, `activo`, `creado_en`) VALUES (4,'20333444','docente2@chispas.edu.ar','$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Pablo','Suárez','DOCENTE',NULL,1,'2026-10-24 21:56:16');
INSERT INTO `usuario` (`id`, `dni`, `email`, `password_hash`, `nombre`, `apellido`, `rol`, `foto_url`, `activo`, `creado_en`) VALUES (5,'46111222',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Lucía','Benítez','ESTUDIANTE',NULL,1,'2026-10-24 21:56:16');

-- Contenidos (la "clase" de cada materia)
INSERT INTO `contenido` (`id`, `espacio_curricular_id`, `tema`, `texto`, `portada_url`, `audio_url`) VALUES (3,2,'Números del 1 al 3','Contamos objetos del aula','https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/500px-Red_Apple.jpg',NULL);
INSERT INTO `contenido` (`id`, `espacio_curricular_id`, `tema`, `texto`, `portada_url`, `audio_url`) VALUES (4,3,'Guerra de Malvinas','L','https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Soldados_argentinos_en_Malvinas%2C_1982.jpg/500px-Soldados_argentinos_en_Malvinas%2C_1982.jpg',NULL);

-- Imágenes
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (15,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/3/37/Theodor_Fontane_Der_Stechlin.jpg/500px-Theodor_Fontane_Der_Stechlin.jpg','un-libro','Un libro',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (16,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/0/08/Pencils_hb.jpg','dos-lapices','Dos lápices',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (17,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/Chairs_at_Museum_of_the_Shenandoah_Valley_-_3.jpg/500px-Chairs_at_Museum_of_the_Shenandoah_Valley_-_3.jpg','tres-sillas','Tres sillas',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (18,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Red_Apple.jpg/500px-Red_Apple.jpg','una-manzana','Una manzana',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (19,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/Two_apples_%2829234068141%29.jpg/500px-Two_apples_%2829234068141%29.jpg','dos-manzanas','Dos manzanas',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (20,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Apples_with_black_background.jpg/500px-Apples_with_black_background.jpg','tres-manzanas','Tres manzanas',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (21,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/5/58/Fuerza-A%C3%A9rea-Argentina_H%C3%A9rcules-C-130-TC-70_19092018_0631.jpg/500px-Fuerza-A%C3%A9rea-Argentina_H%C3%A9rcules-C-130-TC-70_19092018_0631.jpg','avion-hercules','Avión Hércules de la Fuerza Aérea Argentina',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (22,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/ARA_Almirante_Brown_%28D-10%29.jpg/500px-ARA_Almirante_Brown_%28D-10%29.jpg','buque-guerra','Barco de guerra',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (23,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/b/b0/Soldados_argentinos_en_Malvinas%2C_1982.jpg/500px-Soldados_argentinos_en_Malvinas%2C_1982.jpg','soldados-malvinas','Soldados argentinos en Malvinas, 1982',NULL,NULL,1,'2026-10-25 20:00:00');
INSERT INTO `imagen` (`id`, `origen`, `ubicacion`, `nombre`, `texto_alt`, `mime_type`, `tamanio_kb`, `subida_por`, `subida_en`) VALUES (24,'EXTERNA','https://upload.wikimedia.org/wikipedia/commons/thumb/6/64/CABA_-_Retiro_-_Monumento_a_los_Ca%C3%ADdos_en_la_Guerra_de_Malvinas_01.jpg/500px-CABA_-_Retiro_-_Monumento_a_los_Ca%C3%ADdos_en_la_Guerra_de_Malvinas_01.jpg','monumento-malvinas','Monumento a los Caídos en Malvinas',NULL,NULL,1,'2026-10-25 20:00:00');

-- Actividades (plantilla 1 = ORDENAR, 2 = UNIR_FLECHAS)
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (6,3,2,1,'¿Cuántos hay?','Uní cada foto con su número','2026-10-25 20:00:00');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (7,3,1,1,'De menos a más','Ordená de menos a más manzanas','2026-10-25 20:00:00');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (8,4,2,1,'Malvinas 1982','Uní cada foto con su nombre','2026-10-25 20:00:00');
INSERT INTO `actividad` (`id`, `contenido_id`, `plantilla_id`, `creada_por`, `titulo`, `consigna`, `creada_en`) VALUES (9,4,1,1,'¿Qué pasó primero?','Ordená lo que pasó en Malvinas','2026-10-25 20:00:00');

-- Act. 6 UNIR Matemática: IZQ = foto, DER = número (el DER solo muestra el título)
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (40,6,17,'Tres sillas',1,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (41,6,15,'Un libro',2,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (42,6,16,'Dos lápices',3,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (43,6,15,'1',1,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (44,6,16,'2',2,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (45,6,17,'3',3,'DER');
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (12,6,41,43);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (13,6,42,44);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (14,6,40,45);

-- Act. 7 ORDENAR Matemática: nro_orden = posición correcta (el front las mezcla)
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (46,7,18,'Una manzana',1,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (47,7,19,'Dos manzanas',2,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (48,7,20,'Tres manzanas',3,'UNICA');

-- Act. 8 UNIR Historia
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (49,8,22,'Barco',1,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (50,8,23,'Soldados',2,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (51,8,21,'Avión',3,'IZQ');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (52,8,21,'AVIÓN',1,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (53,8,22,'BARCO',2,'DER');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (54,8,23,'SOLDADOS',3,'DER');
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (15,8,51,52);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (16,8,49,53);
INSERT INTO `actividad_par` (`id`, `actividad_id`, `item_izq_id`, `item_der_id`) VALUES (17,8,50,54);

-- Act. 9 ORDENAR Historia
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (55,9,23,'1982: llegan a las islas',1,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (56,9,22,'Llegan los barcos ingleses',2,'UNICA');
INSERT INTO `actividad_imagen` (`id`, `actividad_id`, `imagen_id`, `titulo`, `nro_orden`, `lado`) VALUES (57,9,24,'Hoy recordamos a los héroes',3,'UNICA');

-- Tareas asignadas a Mariel (PENDIENTE, visibles desde hoy)
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (6,6,4,1,'2026-10-25','2026-10-26 20:00:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (7,7,4,1,'2026-10-25','2026-10-26 20:01:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (8,8,5,1,'2026-10-25','2026-10-26 20:02:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
INSERT INTO `tarea` (`id`, `actividad_id`, `carpeta_espacio_id`, `asignada_por`, `fecha_asignacion`, `asignada_en`, `estado`, `iniciada_en`, `finalizada_en`, `duracion_minutos`, `aciertos`, `total_items`) VALUES (9,9,5,1,'2026-10-25','2026-10-26 20:03:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
