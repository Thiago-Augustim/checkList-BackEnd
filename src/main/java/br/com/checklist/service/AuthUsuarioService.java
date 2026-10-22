package br.com.checklist.service;

import br.com.checklist.domain.entity.Usuario;
import br.com.checklist.dto.AuthDtos.*;
import br.com.checklist.exception.BusinessValidationException;
import br.com.checklist.exception.ResourceNotFoundException;
import br.com.checklist.repository.UsuarioRepository;
import br.com.checklist.security.JwtTokenService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuthUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthUsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public AuthResponse autenticar(LoginRequest request) {
        String loginInput = request.username().trim();
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(loginInput)
                .or(() -> usuarioRepository.findByEmailIgnoreCase(loginInput))
                .orElseThrow(() -> new BadCredentialsException("Username ou senha inválidos."));

        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Usuário inativo. Contate o gestor da frota.");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new BadCredentialsException("Username ou senha inválidos.");
        }

        return gerarTokens(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse renovarToken(RefreshRequest request) {
        try {
            UUID userId = jwtTokenService.extractUserId(request.refreshToken());
            Usuario usuario = usuarioRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado para renovação de sessão."));
            return gerarTokens(usuario);
        } catch (Exception e) {
            throw new BadCredentialsException("Refresh token inválido ou expirado.");
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarTodos() {
        return usuarioRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UsuarioDto criarUsuario(CreateUsuarioRequest request) {
        String username = request.username().trim().toLowerCase();
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new BusinessValidationException("Já existe um usuário cadastrado com o username: " + username);
        }

        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessValidationException("Já existe um usuário cadastrado com o e-mail: " + email);
        }

        Usuario novo = new Usuario(
                UUID.randomUUID(),
                request.nome().trim(),
                username,
                email,
                passwordEncoder.encode(request.senha()),
                request.perfil(),
                true
        );

        return toDto(usuarioRepository.save(novo));
    }

    @Transactional
    public UsuarioDto alterarStatusAtivo(UUID id, boolean ativo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuario.setAtivo(ativo);
        return toDto(usuarioRepository.save(usuario));
    }

    private AuthResponse gerarTokens(Usuario usuario) {
        String access = jwtTokenService.generateAccessToken(usuario);
        String refresh = jwtTokenService.generateRefreshToken(usuario);
        return new AuthResponse(access, refresh, toDto(usuario));
    }

    public UsuarioDto toDto(Usuario u) {
        return new UsuarioDto(
                u.getId(),
                u.getNome(),
                u.getUsername(),
                u.getEmail(),
                u.getPerfil(),
                u.isAtivo(),
                u.getCriadoEm()
        );
    }
}
