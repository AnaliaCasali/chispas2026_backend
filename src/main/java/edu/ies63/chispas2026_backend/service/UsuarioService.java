package edu.ies63.chispas2026_backend.service;

import edu.ies63.chispas2026_backend.security.dto.CambiarPasswordRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public interface UsuarioService {
  void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser);
}
