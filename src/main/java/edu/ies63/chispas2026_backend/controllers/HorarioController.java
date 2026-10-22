package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.dtos.MiHorarioDTO;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// pantalla Horarios del alumno. El alumno sale del token, no se manda id
@RestController
@RequestMapping("/api/v1/mi-horario")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class HorarioController {

  private final HorarioService horarioService;

  HorarioController(HorarioService horarioService) {
    this.horarioService = horarioService;
  }

  @GetMapping
  @Operation(summary = "Clases de hoy del alumno ordenadas por hora, con la de ahora marcada")
  public ResponseEntity<MiHorarioDTO> miHorario(@AuthenticationPrincipal Usuario usuario) {
    return ResponseEntity.ok(horarioService.obtenerMiHorario(usuario));
  }
}
