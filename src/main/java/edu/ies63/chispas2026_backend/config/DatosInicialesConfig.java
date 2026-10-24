package edu.ies63.chispas2026_backend.config;

import edu.ies63.chispas2026_backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Carga los datos iniciales (db/chispasinserts.sql) la primera vez que arranca la aplicación.
 * Si ya hay usuarios, no hace nada: es seguro reiniciar el contenedor.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.datos-iniciales.enabled", havingValue = "true", matchIfMissing = true)
public class DatosInicialesConfig {

  private static final String SCRIPT = "db/chispasinserts.sql";

  private final UsuarioRepository usuarioRepository;
  private final DataSource dataSource;

  @Bean
  ApplicationRunner cargarDatosIniciales() {
    return args -> {
      if (usuarioRepository.count() > 0) {
        log.info("Datos iniciales: la base ya tiene datos, no se cargan.");
        return;
      }
      try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
        // todo el script en una sola transacción: si falla un insert no queda la base a medias
        boolean autoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        // El script inserta tablas en orden alfabético: se desactivan las FK mientras corre
        st.execute("SET FOREIGN_KEY_CHECKS=0");
        try {
          ScriptUtils.executeSqlScript(con,
              new EncodedResource(new ClassPathResource(SCRIPT), StandardCharsets.UTF_8));
          con.commit();
        } catch (Exception e) {
          con.rollback();
          throw e;
        } finally {
          st.execute("SET FOREIGN_KEY_CHECKS=1");
          con.setAutoCommit(autoCommit);
        }
      }
      log.info("Datos iniciales: se cargó {} ({} usuarios).", SCRIPT, usuarioRepository.count());
    };
  }
}
