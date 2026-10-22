package br.com.checklist.dto;

import br.com.checklist.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String senha
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record UsuarioDto(
            UUID id,
            String nome,
            String username,
            String email,
            PerfilUsuario perfil,
            boolean ativo,
            OffsetDateTime criadoEm
    ) {
    }

    public record AuthResponse(
            String accessToken,
            String refreshToken,
            UsuarioDto usuario
    ) {
    }

    public record CreateUsuarioRequest(
            @NotBlank String nome,
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank String senha,
            @NotNull PerfilUsuario perfil
    ) {
    }
}
