package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.dtos.InicioDTO;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.InicioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TSDS-6: pantalla de Inicio del alumno. El alumno sale del token, no se manda id
@RestController
@RequestMapping("/api/v1/inicio")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class InicioController {

  private final InicioService inicioService;

  InicioController(InicioService inicioService) {
    this.inicioService = inicioService;
  }

  @GetMapping
  @Operation(summary = "Inicio del alumno: materias (la de ahora primero) con su actividad principal y sus pendientes")
  public ResponseEntity<InicioDTO> inicio(@AuthenticationPrincipal Usuario usuario) {
    return ResponseEntity.ok(inicioService.obtenerInicio(usuario));
  }
}
