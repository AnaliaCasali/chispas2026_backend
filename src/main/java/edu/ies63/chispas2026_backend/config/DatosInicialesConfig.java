package edu.ies63.chispas2026_backend.config;

import edu.ies63.chispas2026_backend.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.sql.SQLException;

// Carga los datos de ejemplo (db/chispasInserts.sql) la primera vez que arranca la app.
// Las tablas ya existen: las crea spring.sql.init con db/chispasEsquema.sql antes de JPA.
// Si ya hay usuarios no hace nada, asi reiniciar el contenedor es seguro.
@Configuration
@ConditionalOnProperty(name = "app.datos-iniciales.enabled", havingValue = "true", matchIfMissing = true)
public class DatosInicialesConfig {

    static final String SCRIPT = "db/chispasInserts.sql";

    private static final Logger log = LoggerFactory.getLogger(DatosInicialesConfig.class);

    @Bean
    ApplicationRunner cargarDatosIniciales(UsuarioRepository usuarioRepository, DataSource dataSource) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                log.info("Datos iniciales: la base ya tiene usuarios, no se cargan.");
                return;
            }
            cargarScript(dataSource);
            log.info("Datos iniciales: se cargo {} ({} usuarios).", SCRIPT, usuarioRepository.count());
        };
    }

    // todo el script en una transaccion: si falla una linea no queda la base a medias
    private void cargarScript(DataSource dataSource) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            boolean autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                ScriptUtils.executeSqlScript(con,
                        new EncodedResource(new ClassPathResource(SCRIPT), StandardCharsets.UTF_8));
                con.commit();
            } catch (RuntimeException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommit);
            }
        }
    }
}
