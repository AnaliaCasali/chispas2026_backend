package edu.ies63.chispas2026_backend.config;

import edu.ies63.chispas2026_backend.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// carga de los datos de ejemplo: solo con la base sin usuarios, y todo o nada
class DatosInicialesConfigTest {

  private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
  private final DataSource dataSource = mock(DataSource.class);
  private final Connection con = mock(Connection.class);
  private final Statement st = mock(Statement.class);

  private ApplicationRunner runner;

  @BeforeEach
  void setUp() throws SQLException {
    when(dataSource.getConnection()).thenReturn(con);
    when(con.createStatement()).thenReturn(st);
    // -1 = no quedan resultados; con el 0 del mock ScriptUtils seguiria pidiendo resultados para siempre
    when(st.getUpdateCount()).thenReturn(-1);
    when(con.getAutoCommit()).thenReturn(true);
    runner = new DatosInicialesConfig().cargarDatosIniciales(usuarioRepository, dataSource);
  }

  @Test
  void conUsuariosNoCargaNada() throws Exception {
    when(usuarioRepository.count()).thenReturn(5L);

    runner.run(null);

    verifyNoInteractions(dataSource);
  }

  @Test
  void sinUsuariosCorreElScriptYConfirma() throws Exception {
    when(usuarioRepository.count()).thenReturn(0L, 5L);

    runner.run(null);

    verify(st, atLeast(20)).execute(anyString());
    verify(con).setAutoCommit(false);
    verify(con).commit();
    verify(con, never()).rollback();
    verify(con).setAutoCommit(true);
  }

  @Test
  void siFallaUnaSentenciaDeshaceTodo() throws Exception {
    when(usuarioRepository.count()).thenReturn(0L);
    when(st.execute(anyString())).thenReturn(false).thenThrow(new SQLException("fk"));

    assertThatThrownBy(() -> runner.run(null)).isInstanceOf(RuntimeException.class);

    verify(con).rollback();
    verify(con, never()).commit();
    verify(con).setAutoCommit(true);
  }
}
