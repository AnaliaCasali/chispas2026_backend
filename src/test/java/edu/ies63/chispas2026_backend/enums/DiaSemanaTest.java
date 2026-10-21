package edu.ies63.chispas2026_backend.enums;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.assertThat;

class DiaSemanaTest {

  @Test
  void deLunesAViernesDevuelveElDiaDeClase() {
    assertThat(DiaSemana.desde(DayOfWeek.MONDAY)).contains(DiaSemana.LUNES);
    assertThat(DiaSemana.desde(DayOfWeek.TUESDAY)).contains(DiaSemana.MARTES);
    assertThat(DiaSemana.desde(DayOfWeek.WEDNESDAY)).contains(DiaSemana.MIERCOLES);
    assertThat(DiaSemana.desde(DayOfWeek.THURSDAY)).contains(DiaSemana.JUEVES);
    assertThat(DiaSemana.desde(DayOfWeek.FRIDAY)).contains(DiaSemana.VIERNES);
  }

  // sabado y domingo no hay clases
  @Test
  void finDeSemanaNoHayClases() {
    assertThat(DiaSemana.desde(DayOfWeek.SATURDAY)).isEmpty();
    assertThat(DiaSemana.desde(DayOfWeek.SUNDAY)).isEmpty();
  }
}
