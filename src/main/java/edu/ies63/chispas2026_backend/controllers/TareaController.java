package edu.ies63.chispas2026_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ies63.chispas2026_backend.dtos.EntregaRequestDTO;
import edu.ies63.chispas2026_backend.dtos.HistorialTareaDTO;
import edu.ies63.chispas2026_backend.dtos.ResultadoTareaDTO;
import edu.ies63.chispas2026_backend.models.Usuario;
import edu.ies63.chispas2026_backend.services.TareaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController 
@RequestMapping("/api/v1/tareas")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class TareaController {

      private final TareaService tareaService;
      
      TareaController(TareaService tareaService){
        this.tareaService = tareaService;
      }

     
      @PostMapping("/{tareaId}/entrega")
      @Operation(summary = "Corregir la entrega de una tarea y guardar el resultado")
      public ResponseEntity<ResultadoTareaDTO> entregar(
          @AuthenticationPrincipal Usuario usuario,
          @PathVariable Long tareaId,
          @Valid @RequestBody EntregaRequestDTO entrega){
        return ResponseEntity.ok(tareaService.entregar(usuario, tareaId, entrega));
      }

      // TSDS-13: el alumno ve el ultimo resultado y el historial de intentos de su tarea
      @GetMapping("/{tareaId}/resultado")
      @Operation(summary = "Ver el último resultado y el historial de intentos de una tarea")
      public ResponseEntity<HistorialTareaDTO> resultado(
          @AuthenticationPrincipal Usuario usuario,
          @PathVariable Long tareaId){
        return ResponseEntity.ok(tareaService.historial(usuario, tareaId));
      }


      
      


}
