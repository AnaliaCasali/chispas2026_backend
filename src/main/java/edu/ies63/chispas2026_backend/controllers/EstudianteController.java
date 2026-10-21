package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.dtos.EstudianteResumenDTO;
import edu.ies63.chispas2026_backend.dtos.PerfilEstudianteDTO;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.EstudianteService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Perfil del estudiante (datos medicos de menores: ninguno es publico). Cada endpoint tiene su rol
@RestController
@RequestMapping("/api/v1/estudiantes")
public class EstudianteController {

  private final EstudianteService estudianteService;

  EstudianteController(EstudianteService estudianteService) {
    this.estudianteService = estudianteService;
  }

  @GetMapping
  @PreAuthorize("hasRole('DOCENTE')")
  @Operation(summary = "Alumnos asignados al docente")
  public ResponseEntity<List<EstudianteResumenDTO>> misAlumnos(@AuthenticationPrincipal Usuario usuario) {
    return ResponseEntity.ok(estudianteService.listarMisAlumnos(usuario));
  }

  // el alumno sale del token, no se manda id
  @GetMapping("/mi-perfil")
  @PreAuthorize("hasRole('ESTUDIANTE')")
  @Operation(summary = "Perfil del alumno logueado")
  public ResponseEntity<PerfilEstudianteDTO> miPerfil(@AuthenticationPrincipal Usuario usuario) {
    return ResponseEntity.ok(estudianteService.obtenerPerfil(usuario, usuario.getId()));
  }

  // el docente ve a sus alumnos y el alumno solo a si mismo, si no 403
  @GetMapping("/{estudianteId}/perfil")
  @PreAuthorize("hasAnyRole('DOCENTE', 'ESTUDIANTE')")
  @Operation(summary = "Perfil de un estudiante con su curso y sus condiciones")
  public ResponseEntity<PerfilEstudianteDTO> perfil(
      @AuthenticationPrincipal Usuario usuario,
      @PathVariable Long estudianteId) {
    return ResponseEntity.ok(estudianteService.obtenerPerfil(usuario, estudianteId));
  }
}
