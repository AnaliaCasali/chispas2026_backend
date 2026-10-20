package edu.ies63.chispas2026_backend.enums;

import java.time.DayOfWeek;
import java.util.Optional;

public enum DiaSemana {
  LUNES, MARTES, MIERCOLES, JUEVES, VIERNES;

  // sabado y domingo no hay clases
  public static Optional<DiaSemana> desde(DayOfWeek dia) {
    return switch (dia) {
      case MONDAY -> Optional.of(LUNES);
      case TUESDAY -> Optional.of(MARTES);
      case WEDNESDAY -> Optional.of(MIERCOLES);
      case THURSDAY -> Optional.of(JUEVES);
      case FRIDAY -> Optional.of(VIERNES);
      case SATURDAY, SUNDAY -> Optional.empty();
    };
  }
}
