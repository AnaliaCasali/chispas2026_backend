package edu.ies63.chispas2026_backend.services;

import edu.ies63.chispas2026_backend.dtos.InicioDTO;
import edu.ies63.chispas2026_backend.models.Usuario;

// TSDS-6: resumen del Inicio del alumno logueado
public interface InicioService {
  InicioDTO obtenerInicio(Usuario estudiante);
}
