package edu.ies63.chispas2026_backend.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
  public UsuarioNoEncontradoException(Long id) {
    super("Usuario con id " + id + " no encontrado");
  }
  public UsuarioNoEncontradoException(String username) {super("Usuario no encontrado: " + username);}
}