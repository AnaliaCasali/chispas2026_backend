package edu.ies63.chispas2026_backend.security.auth;

import edu.ies63.chispas2026_backend.dtos.UsuarioCreateDTO;
import edu.ies63.chispas2026_backend.security.dto.AuthRequest;
import edu.ies63.chispas2026_backend.security.dto.AuthResponse;
import edu.ies63.chispas2026_backend.security.dto.ForgotPasswordRequest;
import edu.ies63.chispas2026_backend.security.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Iniciar sesión y obtener JWT")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  @PostMapping("/register/usuario")
  @Operation(summary = "Registrar usuario estándar")
  public ResponseEntity<AuthResponse> registerUsuario(
      @Valid @RequestBody UsuarioCreateDTO request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(authService.registerUsuario(request));
  }


  @PostMapping("/forgot-password")
  @Operation(summary = "Olvide Contraseña")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.requestPasswordReset(request);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reiniciar Contraseña Olvidada")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request);
    return ResponseEntity.ok().build();
  }
}
