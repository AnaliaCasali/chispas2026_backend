-- MySQL dump 10.13  Distrib 8.0.36, for Win64 (x86_64)
--
-- Host: localhost    Database: chispas
-- ------------------------------------------------------
-- Server version	8.0.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actividad`
--

DROP TABLE IF EXISTS `actividad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actividad` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `contenido_id` bigint unsigned NOT NULL,
  `plantilla_id` bigint unsigned NOT NULL,
  `creada_por` bigint unsigned NOT NULL,
  `titulo` varchar(100) NOT NULL,
  `consigna` varchar(255) NOT NULL,
  `creada_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_act_cont` (`contenido_id`),
  KEY `fk_act_plant` (`plantilla_id`),
  KEY `fk_act_doc` (`creada_por`),
  CONSTRAINT `fk_act_cont` FOREIGN KEY (`contenido_id`) REFERENCES `contenido` (`id`),
  CONSTRAINT `fk_act_doc` FOREIGN KEY (`creada_por`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_act_plant` FOREIGN KEY (`plantilla_id`) REFERENCES `plantilla` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actividad`
--

LOCK TABLES `actividad` WRITE;
/*!40000 ALTER TABLE `actividad` DISABLE KEYS */;
INSERT INTO `actividad` VALUES (1,1,1,1,'Ordenar la historia','Arrastrá las imágenes en orden','2026-10-20 21:17:35'),(2,2,1,1,'Mi mañana','Ordená lo que hacés al levantarte','2026-10-20 21:17:35'),(3,1,2,1,'Unir con flechas','Uní cada imagen con la que corresponde','2026-10-20 21:17:35'),(4,2,2,1,'¿Qué uso para...?','Uní cada acción con su objeto','2026-10-20 21:17:35');
/*!40000 ALTER TABLE `actividad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actividad_imagen`
--

DROP TABLE IF EXISTS `actividad_imagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actividad_imagen` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `actividad_id` bigint unsigned NOT NULL,
  `imagen_id` bigint unsigned NOT NULL,
  `titulo` varchar(80) DEFAULT NULL,
  `nro_orden` int unsigned NOT NULL,
  `lado` enum('UNICA','IZQ','DER') NOT NULL DEFAULT 'UNICA',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ai_imagen` (`actividad_id`,`imagen_id`),
  UNIQUE KEY `uq_ai_orden` (`actividad_id`,`lado`,`nro_orden`),
  KEY `fk_ai_img` (`imagen_id`),
  CONSTRAINT `fk_ai_act` FOREIGN KEY (`actividad_id`) REFERENCES `actividad` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ai_img` FOREIGN KEY (`imagen_id`) REFERENCES `imagen` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actividad_imagen`
--

LOCK TABLES `actividad_imagen` WRITE;
/*!40000 ALTER TABLE `actividad_imagen` DISABLE KEYS */;
INSERT INTO `actividad_imagen` VALUES (1,1,2,'Don Quijote lee',1,'UNICA'),(2,1,1,'Sale de su casa',2,'UNICA'),(3,1,4,'Vs Molinos',3,'UNICA'),(4,1,3,'Vuelve a casa',4,'UNICA'),(5,2,9,'Suena el despertador',1,'UNICA'),(6,2,10,'Me lavo los dientes',2,'UNICA'),(7,2,11,'Desayuno',3,'UNICA'),(8,2,12,'Preparo la mochila',4,'UNICA'),(9,2,13,'Voy a la escuela',5,'UNICA'),(10,3,5,'Sueño',1,'IZQ'),(11,3,7,'Llave',2,'IZQ'),(12,3,8,'Puerta',1,'DER'),(13,3,6,'Cama',2,'DER'),(14,4,14,'Dientes limpios',1,'IZQ'),(15,4,5,'Tengo sueño',2,'IZQ'),(16,4,13,'Ir a estudiar',3,'IZQ'),(17,4,6,'Cama',1,'DER'),(18,4,12,'Mochila',2,'DER'),(19,4,10,'Cepillo',3,'DER');
/*!40000 ALTER TABLE `actividad_imagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `actividad_par`
--

DROP TABLE IF EXISTS `actividad_par`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actividad_par` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `actividad_id` bigint unsigned NOT NULL,
  `item_izq_id` bigint unsigned NOT NULL,
  `item_der_id` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_par_izq` (`actividad_id`,`item_izq_id`),
  UNIQUE KEY `uq_par_der` (`actividad_id`,`item_der_id`),
  KEY `fk_par_izq` (`item_izq_id`),
  KEY `fk_par_der` (`item_der_id`),
  CONSTRAINT `fk_par_act` FOREIGN KEY (`actividad_id`) REFERENCES `actividad` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_par_der` FOREIGN KEY (`item_der_id`) REFERENCES `actividad_imagen` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_par_izq` FOREIGN KEY (`item_izq_id`) REFERENCES `actividad_imagen` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actividad_par`
--

LOCK TABLES `actividad_par` WRITE;
/*!40000 ALTER TABLE `actividad_par` DISABLE KEYS */;
INSERT INTO `actividad_par` VALUES (1,3,10,13),(2,3,11,12),(3,4,14,19),(4,4,15,17),(5,4,16,18);
/*!40000 ALTER TABLE `actividad_par` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `capacidad`
--

DROP TABLE IF EXISTS `capacidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `capacidad` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `area` varchar(60) NOT NULL,
  `pregunta` varchar(255) NOT NULL,
  `orden` int unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `capacidad`
--

LOCK TABLES `capacidad` WRITE;
/*!40000 ALTER TABLE `capacidad` DISABLE KEYS */;
INSERT INTO `capacidad` VALUES (1,'Comunicación y Comprensión','¿Se comunica mediante lenguaje oral?',1),(2,'Lectura y Escritura','¿Se encuentra alfabetizado/a o realiza lectura convencional?',2),(3,'Nociones Matemáticas','¿Reconoce o escribe números de manera autónoma?',3),(4,'Atención','¿Mantiene periodos de atención breves que requieren actividades cortas?',4);
/*!40000 ALTER TABLE `capacidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carpeta`
--

DROP TABLE IF EXISTS `carpeta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carpeta` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `estudiante_id` bigint unsigned NOT NULL,
  `ciclo_lectivo_id` bigint unsigned NOT NULL,
  `curso_id` bigint unsigned NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `color` char(7) DEFAULT NULL,
  `creada_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_carpeta` (`estudiante_id`,`ciclo_lectivo_id`),
  KEY `fk_carp_ciclo` (`ciclo_lectivo_id`),
  KEY `fk_carp_curso` (`curso_id`),
  CONSTRAINT `fk_carp_ciclo` FOREIGN KEY (`ciclo_lectivo_id`) REFERENCES `ciclo_lectivo` (`id`),
  CONSTRAINT `fk_carp_curso` FOREIGN KEY (`curso_id`) REFERENCES `curso` (`id`),
  CONSTRAINT `fk_carp_est` FOREIGN KEY (`estudiante_id`) REFERENCES `estudiante` (`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carpeta`
--

LOCK TABLES `carpeta` WRITE;
/*!40000 ALTER TABLE `carpeta` DISABLE KEYS */;
INSERT INTO `carpeta` VALUES (1,2,1,2,'Carpeta Celeste','#7BC4E8','2026-10-20 21:17:35'),(2,2,2,1,'Carpeta Rosa','#E87BB0','2026-10-20 21:17:35'),(3,3,2,1,'Carpeta Verde','#7BE89A','2026-10-20 21:17:35');
/*!40000 ALTER TABLE `carpeta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carpeta_espacio`
--

DROP TABLE IF EXISTS `carpeta_espacio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carpeta_espacio` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `carpeta_id` bigint unsigned NOT NULL,
  `espacio_curricular_id` bigint unsigned NOT NULL,
  `orden` int unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_carpeta_espacio` (`carpeta_id`,`espacio_curricular_id`),
  KEY `fk_ce_espacio` (`espacio_curricular_id`),
  CONSTRAINT `fk_ce_carpeta` FOREIGN KEY (`carpeta_id`) REFERENCES `carpeta` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ce_espacio` FOREIGN KEY (`espacio_curricular_id`) REFERENCES `espacio_curricular` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carpeta_espacio`
--

LOCK TABLES `carpeta_espacio` WRITE;
/*!40000 ALTER TABLE `carpeta_espacio` DISABLE KEYS */;
INSERT INTO `carpeta_espacio` VALUES (1,1,1,1),(2,1,2,2),(3,2,1,1),(4,2,2,2),(5,2,3,3),(6,3,1,1),(7,3,2,2);
/*!40000 ALTER TABLE `carpeta_espacio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `idCategoria` int NOT NULL AUTO_INCREMENT,
  `categoria` varchar(145) DEFAULT NULL,
  PRIMARY KEY (`idCategoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ciclo_lectivo`
--

DROP TABLE IF EXISTS `ciclo_lectivo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ciclo_lectivo` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `anio` int NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `anio` (`anio`),
  CONSTRAINT `ck_ciclo_fechas` CHECK ((`fecha_fin` > `fecha_inicio`))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ciclo_lectivo`
--

LOCK TABLES `ciclo_lectivo` WRITE;
/*!40000 ALTER TABLE `ciclo_lectivo` DISABLE KEYS */;
INSERT INTO `ciclo_lectivo` VALUES (1,2025,'2025-03-03','2025-12-12',0),(2,2026,'2026-03-02','2026-12-11',1);
/*!40000 ALTER TABLE `ciclo_lectivo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `condicion`
--

DROP TABLE IF EXISTS `condicion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `condicion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe2onqpcs2kugqr9s3b4x1a1va` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `condicion`
--

LOCK TABLES `condicion` WRITE;
/*!40000 ALTER TABLE `condicion` DISABLE KEYS */;
/*!40000 ALTER TABLE `condicion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contenido`
--

DROP TABLE IF EXISTS `contenido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contenido` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `espacio_curricular_id` bigint unsigned NOT NULL,
  `tema` varchar(120) NOT NULL,
  `texto` varchar(255) NOT NULL,
  `portada_url` varchar(255) DEFAULT NULL,
  `audio_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cont_espacio` (`espacio_curricular_id`),
  CONSTRAINT `fk_cont_espacio` FOREIGN KEY (`espacio_curricular_id`) REFERENCES `espacio_curricular` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contenido`
--

LOCK TABLES `contenido` WRITE;
/*!40000 ALTER TABLE `contenido` DISABLE KEYS */;
INSERT INTO `contenido` VALUES (1,1,'Literatura Española','Don Quijote de la Mancha','https://picsum.photos/seed/quijote-portada/400/600',NULL),(2,1,'Vida cotidiana','Rutina de la mañana','https://picsum.photos/seed/rutina-portada/400/600',NULL);
/*!40000 ALTER TABLE `contenido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `curso`
--

DROP TABLE IF EXISTS `curso`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `curso` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `ciclo_lectivo_id` bigint unsigned NOT NULL,
  `nombre` varchar(30) NOT NULL,
  `division` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_curso` (`ciclo_lectivo_id`,`nombre`,`division`),
  CONSTRAINT `fk_curso_ciclo` FOREIGN KEY (`ciclo_lectivo_id`) REFERENCES `ciclo_lectivo` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `curso`
--

LOCK TABLES `curso` WRITE;
/*!40000 ALTER TABLE `curso` DISABLE KEYS */;
INSERT INTO `curso` VALUES (2,1,'3er Año','A'),(1,2,'4to Año','A');
/*!40000 ALTER TABLE `curso` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `espacio_curricular`
--

DROP TABLE IF EXISTS `espacio_curricular`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `espacio_curricular` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `nombre` varchar(60) NOT NULL,
  `icono` varchar(255) DEFAULT NULL,
  `color` char(7) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `espacio_curricular`
--

LOCK TABLES `espacio_curricular` WRITE;
/*!40000 ALTER TABLE `espacio_curricular` DISABLE KEYS */;
INSERT INTO `espacio_curricular` VALUES (1,'Lengua y Literatura',NULL,'#4F7BE8'),(2,'Matemática',NULL,'#E85F7B'),(3,'Historia',NULL,'#C08A3E');
/*!40000 ALTER TABLE `espacio_curricular` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estudiante`
--

DROP TABLE IF EXISTS `estudiante`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estudiante` (
  `usuario_id` bigint unsigned NOT NULL,
  `observaciones` text,
  PRIMARY KEY (`usuario_id`),
  CONSTRAINT `fk_est_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estudiante`
--

LOCK TABLES `estudiante` WRITE;
/*!40000 ALTER TABLE `estudiante` DISABLE KEYS */;
INSERT INTO `estudiante` VALUES (2,'Mariel accede mejor a los aprendizajes cuando la propuesta se presenta de manera concreta, visual, breve y mediante demostración, con pocas opciones y posibilidades de respuesta que no dependan de la lectura, la escritura o el lenguaje oral.'),(3,NULL);
/*!40000 ALTER TABLE `estudiante` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estudiante_capacidad`
--

DROP TABLE IF EXISTS `estudiante_capacidad`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estudiante_capacidad` (
  `estudiante_id` bigint unsigned NOT NULL,
  `capacidad_id` bigint unsigned NOT NULL,
  `respuesta` tinyint(1) NOT NULL,
  `actualizado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`estudiante_id`,`capacidad_id`),
  KEY `fk_ec_cap` (`capacidad_id`),
  CONSTRAINT `fk_ec_cap` FOREIGN KEY (`capacidad_id`) REFERENCES `capacidad` (`id`),
  CONSTRAINT `fk_ec_est` FOREIGN KEY (`estudiante_id`) REFERENCES `estudiante` (`usuario_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estudiante_capacidad`
--

LOCK TABLES `estudiante_capacidad` WRITE;
/*!40000 ALTER TABLE `estudiante_capacidad` DISABLE KEYS */;
INSERT INTO `estudiante_capacidad` VALUES (2,1,1,'2026-10-20 21:17:35'),(2,2,0,'2026-10-20 21:17:35'),(2,3,1,'2026-10-20 21:17:35'),(2,4,1,'2026-10-20 21:17:35');
/*!40000 ALTER TABLE `estudiante_capacidad` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estudiante_condicion`
--

DROP TABLE IF EXISTS `estudiante_condicion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estudiante_condicion` (
  `estudiante_id` bigint NOT NULL,
  `condicion_id` bigint NOT NULL,
  PRIMARY KEY (`estudiante_id`,`condicion_id`),
  KEY `FKt6mqpffrc31qr1sj813f8n1j8` (`condicion_id`),
  CONSTRAINT `FKt6mqpffrc31qr1sj813f8n1j8` FOREIGN KEY (`condicion_id`) REFERENCES `condicion` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estudiante_condicion`
--

LOCK TABLES `estudiante_condicion` WRITE;
/*!40000 ALTER TABLE `estudiante_condicion` DISABLE KEYS */;
/*!40000 ALTER TABLE `estudiante_condicion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `horario`
--

DROP TABLE IF EXISTS `horario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `horario` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `curso_id` bigint unsigned NOT NULL,
  `espacio_curricular_id` bigint unsigned NOT NULL,
  `docente_id` bigint unsigned DEFAULT NULL,
  `dia` enum('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES') NOT NULL,
  `hora_inicio` time NOT NULL,
  `hora_fin` time NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_horario` (`curso_id`,`dia`,`hora_inicio`),
  KEY `fk_hor_espacio` (`espacio_curricular_id`),
  KEY `fk_hor_docente` (`docente_id`),
  CONSTRAINT `fk_hor_curso` FOREIGN KEY (`curso_id`) REFERENCES `curso` (`id`),
  CONSTRAINT `fk_hor_docente` FOREIGN KEY (`docente_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_hor_espacio` FOREIGN KEY (`espacio_curricular_id`) REFERENCES `espacio_curricular` (`id`),
  CONSTRAINT `ck_horario_horas` CHECK ((`hora_fin` > `hora_inicio`))
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `horario`
--

LOCK TABLES `horario` WRITE;
/*!40000 ALTER TABLE `horario` DISABLE KEYS */;
INSERT INTO `horario` VALUES (1,1,1,1,'LUNES','08:45:00','09:30:00'),(2,1,2,NULL,'LUNES','09:30:00','10:15:00'),(3,1,3,NULL,'LUNES','10:15:00','11:00:00'),(4,1,2,NULL,'MARTES','08:45:00','09:30:00'),(5,1,3,NULL,'MARTES','09:30:00','10:15:00'),(6,1,1,1,'MARTES','10:15:00','11:00:00'),(7,1,3,NULL,'MIERCOLES','08:45:00','09:30:00'),(8,1,1,1,'MIERCOLES','09:30:00','10:15:00'),(9,1,2,NULL,'MIERCOLES','10:15:00','11:00:00'),(10,1,1,1,'JUEVES','08:45:00','09:30:00'),(11,1,2,NULL,'JUEVES','09:30:00','10:15:00'),(12,1,3,NULL,'JUEVES','10:15:00','11:00:00'),(13,1,2,NULL,'VIERNES','08:45:00','09:30:00'),(14,1,1,1,'VIERNES','09:30:00','10:15:00'),(15,1,3,NULL,'VIERNES','10:15:00','11:00:00');
/*!40000 ALTER TABLE `horario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imagen`
--

DROP TABLE IF EXISTS `imagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagen` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `origen` enum('LOCAL','EXTERNA') NOT NULL,
  `ubicacion` varchar(255) NOT NULL,
  `nombre` varchar(80) DEFAULT NULL,
  `texto_alt` varchar(160) NOT NULL,
  `mime_type` varchar(30) DEFAULT NULL,
  `tamanio_kb` int unsigned DEFAULT NULL,
  `subida_por` bigint unsigned NOT NULL,
  `subida_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ubicacion` (`ubicacion`),
  KEY `fk_img_usuario` (`subida_por`),
  CONSTRAINT `fk_img_usuario` FOREIGN KEY (`subida_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imagen`
--

LOCK TABLES `imagen` WRITE;
/*!40000 ALTER TABLE `imagen` DISABLE KEYS */;
INSERT INTO `imagen` VALUES (1,'EXTERNA','https://picsum.photos/seed/quijote-sale/400/300','quijote-sale','Don Quijote sale de su casa a caballo',NULL,NULL,1,'2026-10-20 21:17:35'),(2,'EXTERNA','https://picsum.photos/seed/quijote-lee/400/300','quijote-lee','Don Quijote lee libros de caballería',NULL,NULL,1,'2026-10-20 21:17:35'),(3,'EXTERNA','https://picsum.photos/seed/quijote-vuelve/400/300','quijote-vuelve','Don Quijote vuelve a su casa',NULL,NULL,1,'2026-10-20 21:17:35'),(4,'EXTERNA','https://picsum.photos/seed/quijote-molinos/400/300','quijote-molinos','Don Quijote enfrenta a los molinos de viento',NULL,NULL,1,'2026-10-20 21:17:35'),(5,'EXTERNA','https://picsum.photos/seed/bostezo/300/300','bostezo','Persona bostezando con sueño',NULL,NULL,1,'2026-10-20 21:17:35'),(6,'EXTERNA','https://picsum.photos/seed/cama/300/300','cama','Cama en un dormitorio',NULL,NULL,1,'2026-10-20 21:17:35'),(7,'EXTERNA','https://picsum.photos/seed/llave/300/300','llave','Llave antigua',NULL,NULL,1,'2026-10-20 21:17:35'),(8,'EXTERNA','https://picsum.photos/seed/puerta/300/300','puerta','Puerta de madera',NULL,NULL,1,'2026-10-20 21:17:35'),(9,'EXTERNA','https://picsum.photos/seed/despertador/300/300','despertador','Reloj despertador sonando',NULL,NULL,1,'2026-10-20 21:17:35'),(10,'EXTERNA','https://picsum.photos/seed/cepillo/300/300','cepillo','Cepillo de dientes',NULL,NULL,1,'2026-10-20 21:17:35'),(11,'EXTERNA','https://picsum.photos/seed/desayuno/300/300','desayuno','Taza de leche y tostadas',NULL,NULL,1,'2026-10-20 21:17:35'),(12,'EXTERNA','https://picsum.photos/seed/mochila/300/300','mochila','Mochila escolar',NULL,NULL,1,'2026-10-20 21:17:35'),(13,'EXTERNA','https://picsum.photos/seed/escuela/300/300','escuela','Edificio de una escuela',NULL,NULL,1,'2026-10-20 21:17:35'),(14,'EXTERNA','https://picsum.photos/seed/dientes/300/300','dientes','Sonrisa con dientes limpios',NULL,NULL,1,'2026-10-20 21:17:35');
/*!40000 ALTER TABLE `imagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `password_reset_tokens`
--

DROP TABLE IF EXISTS `password_reset_tokens`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(255) NOT NULL,
  `usuario_id` bigint unsigned NOT NULL,
  `expiry_date` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  UNIQUE KEY `usuario_id` (`usuario_id`),
  CONSTRAINT `fk_prt_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuario` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `password_reset_tokens`
--

LOCK TABLES `password_reset_tokens` WRITE;
/*!40000 ALTER TABLE `password_reset_tokens` DISABLE KEYS */;
/*!40000 ALTER TABLE `password_reset_tokens` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedidos` (
  `idPedido` int NOT NULL AUTO_INCREMENT,
  `estado` varchar(45) NOT NULL,
  `fechaPedido` date NOT NULL,
  `idUsuario` int NOT NULL,
  PRIMARY KEY (`idPedido`,`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedidos`
--

LOCK TABLES `pedidos` WRITE;
/*!40000 ALTER TABLE `pedidos` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedidos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plantilla`
--

DROP TABLE IF EXISTS `plantilla`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plantilla` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `codigo` enum('ORDENAR','UNIR_FLECHAS') NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `destinatario` varchar(120) DEFAULT NULL,
  `min_items` int unsigned NOT NULL,
  `max_items` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `codigo` (`codigo`),
  CONSTRAINT `ck_plantilla_items` CHECK ((`max_items` >= `min_items`))
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plantilla`
--

LOCK TABLES `plantilla` WRITE;
/*!40000 ALTER TABLE `plantilla` DISABLE KEYS */;
INSERT INTO `plantilla` VALUES (1,'ORDENAR','Ordenar la historia','Secuencia temporal',3,8),(2,'UNIR_FLECHAS','Relación con imágenes','Asociación objeto-acción',2,5);
/*!40000 ALTER TABLE `plantilla` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `idProducto` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(45) NOT NULL,
  `precio` varchar(45) NOT NULL,
  `descripcion` varchar(45) NOT NULL,
  `urlImagen` varchar(45) NOT NULL,
  `urlML` varchar(145) NOT NULL,
  `idCategoria` int DEFAULT NULL,
  PRIMARY KEY (`idProducto`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'llavero','5000','Sombrelo Luffy','dfqwefwqe','fqwefwe',2);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos_por_pedido`
--

DROP TABLE IF EXISTS `productos_por_pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos_por_pedido` (
  `idProducto` int NOT NULL,
  `idPedido` int NOT NULL,
  `cantidad` int DEFAULT NULL,
  `precioUnitario` double DEFAULT NULL,
  PRIMARY KEY (`idProducto`,`idPedido`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos_por_pedido`
--

LOCK TABLES `productos_por_pedido` WRITE;
/*!40000 ALTER TABLE `productos_por_pedido` DISABLE KEYS */;
/*!40000 ALTER TABLE `productos_por_pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `respuesta`
--

DROP TABLE IF EXISTS `respuesta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `respuesta` (
  `tarea_id` bigint unsigned NOT NULL,
  `item_id` bigint unsigned NOT NULL,
  `posicion_elegida` int unsigned DEFAULT NULL,
  `item_unido_id` bigint unsigned DEFAULT NULL,
  `es_correcta` tinyint(1) NOT NULL,
  `intento` int NOT NULL,
  PRIMARY KEY (`tarea_id`,`item_id`),
  KEY `fk_resp_item` (`item_id`),
  KEY `fk_resp_union` (`item_unido_id`),
  CONSTRAINT `fk_resp_item` FOREIGN KEY (`item_id`) REFERENCES `actividad_imagen` (`id`),
  CONSTRAINT `fk_resp_tarea` FOREIGN KEY (`tarea_id`) REFERENCES `tarea` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_resp_union` FOREIGN KEY (`item_unido_id`) REFERENCES `actividad_imagen` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `respuesta`
--

LOCK TABLES `respuesta` WRITE;
/*!40000 ALTER TABLE `respuesta` DISABLE KEYS */;
INSERT INTO `respuesta` VALUES (1,1,1,NULL,1,0),(1,2,2,NULL,1,0),(1,3,4,NULL,0,0),(1,4,3,NULL,0,0);
/*!40000 ALTER TABLE `respuesta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tarea`
--

DROP TABLE IF EXISTS `tarea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tarea` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `actividad_id` bigint unsigned NOT NULL,
  `carpeta_espacio_id` bigint unsigned NOT NULL,
  `asignada_por` bigint unsigned NOT NULL,
  `fecha_asignacion` date NOT NULL,
  `asignada_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `estado` enum('PENDIENTE','EN_CURSO','FINALIZADA') NOT NULL DEFAULT 'PENDIENTE',
  `iniciada_en` datetime DEFAULT NULL,
  `finalizada_en` datetime DEFAULT NULL,
  `duracion_minutos` int unsigned DEFAULT NULL,
  `aciertos` int unsigned DEFAULT NULL,
  `total_items` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tarea` (`actividad_id`,`carpeta_espacio_id`,`fecha_asignacion`),
  KEY `fk_tar_doc` (`asignada_por`),
  KEY `ix_tarea_ce_fecha` (`carpeta_espacio_id`,`fecha_asignacion`,`asignada_en`),
  CONSTRAINT `fk_tar_act` FOREIGN KEY (`actividad_id`) REFERENCES `actividad` (`id`),
  CONSTRAINT `fk_tar_ce` FOREIGN KEY (`carpeta_espacio_id`) REFERENCES `carpeta_espacio` (`id`),
  CONSTRAINT `fk_tar_doc` FOREIGN KEY (`asignada_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tarea`
--

LOCK TABLES `tarea` WRITE;
/*!40000 ALTER TABLE `tarea` DISABLE KEYS */;
INSERT INTO `tarea` VALUES (1,1,3,1,'2026-10-16','2026-10-16 08:45:00','FINALIZADA','2026-10-16 08:50:00','2026-10-16 08:57:00',7,2,4),(2,3,3,1,'2026-10-16','2026-10-16 08:30:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL),(3,2,6,1,'2026-10-16','2026-10-16 08:40:00','PENDIENTE',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `tarea` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `dni` varchar(12) NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `password_hash` varchar(100) NOT NULL,
  `nombre` varchar(60) NOT NULL,
  `apellido` varchar(60) NOT NULL,
  `rol` enum('ESTUDIANTE','DOCENTE','PRECEPTOR','ADMIN') NOT NULL,
  `foto_url` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `creado_en` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dni` (`dni`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'20111222','docente@chispas.edu.ar','$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Laura','Gómez','DOCENTE',NULL,1,'2026-10-20 21:17:35'),(2,'45123456',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Mariel','Linera','ESTUDIANTE','https://i.ibb.co/21dkkQLq/IMG-20261019-WA0013.jpg',1,'2026-10-20 21:17:35'),(3,'45654321',NULL,'$2a$10$a4loYKgOGxdGNrl8iJmGkeEoJhtvsOzvF.x00vh8OTQPT6OoTF39K','Tomás','Ríos','ESTUDIANTE',NULL,1,'2026-10-20 21:17:35');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usurios`
--

DROP TABLE IF EXISTS `usurios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usurios` (
  `idUsuario` int NOT NULL,
  `nombre` varchar(145) NOT NULL,
  `clave` varchar(8) NOT NULL,
  PRIMARY KEY (`idUsuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usurios`
--

LOCK TABLES `usurios` WRITE;
/*!40000 ALTER TABLE `usurios` DISABLE KEYS */;
/*!40000 ALTER TABLE `usurios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-24 18:22:45
