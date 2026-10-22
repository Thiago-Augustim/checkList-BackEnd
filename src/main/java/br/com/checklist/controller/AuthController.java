package br.com.checklist.controller;

import br.com.checklist.dto.AuthDtos.*;
import br.com.checklist.service.AuthUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthUsuarioService authUsuarioService;

    public AuthController(AuthUsuarioService authUsuarioService) {
        this.authUsuarioService = authUsuarioService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUsuarioService.autenticar(request));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authUsuarioService.renovarToken(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        return ResponseEntity.ok(authUsuarioService.listarTodos());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDto> criarUsuario(@Valid @RequestBody CreateUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authUsuarioService.criarUsuario(request));
    }

    @PatchMapping("/usuarios/{id}/status")
    public ResponseEntity<UsuarioDto> alterarStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        return ResponseEntity.ok(authUsuarioService.alterarStatusAtivo(id, ativo));
    }
}
