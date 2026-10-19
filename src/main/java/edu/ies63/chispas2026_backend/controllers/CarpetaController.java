package edu.ies63.chispas2026_backend.controllers;

import edu.ies63.chispas2026_backend.dtos.ActividadesMateriaDTO;
import edu.ies63.chispas2026_backend.dtos.MiCarpetaDTO;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.CarpetaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// el alumno lo saco del token, no se manda id
@RestController
@RequestMapping("/api/v1/mi-carpeta")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class CarpetaController {

  private final CarpetaService carpetaService;

  CarpetaController(CarpetaService carpetaService) {
    this.carpetaService = carpetaService;
  }

  @GetMapping
  @Operation(summary = "Carpeta del alumno con la materia actual destacada")
  public ResponseEntity<MiCarpetaDTO> miCarpeta(@AuthenticationPrincipal Usuario usuario) {
    return ResponseEntity.ok(carpetaService.obtenerMiCarpeta(usuario));
  }

  @GetMapping("/materias/{carpetaEspacioId}/actividades")
  @Operation(summary = "Actividades asignadas en una materia de la carpeta del alumno")
  public ResponseEntity<ActividadesMateriaDTO> actividadesDeMateria(
      @AuthenticationPrincipal Usuario usuario,
      @PathVariable Long carpetaEspacioId) {
    return ResponseEntity.ok(carpetaService.obtenerActividadesDeMateria(usuario, carpetaEspacioId));
  }
}
