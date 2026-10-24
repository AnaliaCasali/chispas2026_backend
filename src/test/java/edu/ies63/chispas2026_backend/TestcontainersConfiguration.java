package edu.ies63.chispas2026_backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

// Base de los tests: MySQL en un contenedor (Testcontainers) con las tablas y los datos de ejemplo
// (db/chispasEsquema.sql y db/chispasInserts.sql), la misma base que usa produccion.
// Solo hace falta Docker: no usa el .env ni un MySQL local.
// Se importa con @Import(TestcontainersConfiguration.class) en cada test que levanta Spring.
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

  // un solo contenedor para toda la corrida (static), asi no arranca un MySQL por cada contexto
  static final MySQLContainer MYSQL = new MySQLContainer(DockerImageName.parse("mysql:8.4"))
      .withDatabaseName("chispas")
      // MySQL los corre en orden al crear el contenedor: primero las tablas, despues los datos
      .withCopyFileToContainer(
          MountableFile.forClasspathResource("db/chispasEsquema.sql"),
          "/docker-entrypoint-initdb.d/01-esquema.sql")
      .withCopyFileToContainer(
          MountableFile.forClasspathResource("db/chispasInserts.sql"),
          "/docker-entrypoint-initdb.d/02-inserts.sql")
      .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci");

  // @ServiceConnection completa spring.datasource.url, username y password con los del contenedor
  @Bean
  @ServiceConnection
  MySQLContainer mysqlContainer() {
    return MYSQL;
  }

  // valores fijos de prueba: pisan los del .env y los de las variables de entorno
  @Bean
  DynamicPropertyRegistrar propiedadesDePrueba() {
    return registry -> {
      registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
      registry.add("spring.jpa.show-sql", () -> "false");
      // base64 de 32 bytes, solo para tests
      registry.add("app.security.jwt.secret", () -> "Y2hpc3Bhcy1jaS1jbGF2ZS1kZS1wcnVlYmEtMzItYnl0ZXM=");
      registry.add("spring.mail.username", () -> "tests@example.com");
      registry.add("spring.mail.password", () -> "tests");
    };
  }
}
