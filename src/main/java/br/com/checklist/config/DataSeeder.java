package br.com.checklist.config;

import br.com.checklist.domain.entity.Usuario;
import br.com.checklist.domain.enums.PerfilUsuario;
import br.com.checklist.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataSeeder implements ApplicationRunner {

    public static final UUID DEFAULT_GESTOR_ID = UUID.fromString("110e8400-e29b-41d4-a716-446655440001");
    public static final UUID DEFAULT_MOTORISTA_ID = UUID.fromString("110e8400-e29b-41d4-a716-446655440002");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByUsernameIgnoreCase("gestor")
                && !usuarioRepository.existsByEmailIgnoreCase("gestor@checklist.com.br")) {
            Usuario gestor = new Usuario(
                    DEFAULT_GESTOR_ID,
                    "Carlos Mendes (Gestor de Frota)",
                    "gestor",
                    "gestor@checklist.com.br",
                    passwordEncoder.encode("senha123"),
                    PerfilUsuario.GESTOR,
                    true
            );
            usuarioRepository.save(gestor);
        }

        if (!usuarioRepository.existsByUsernameIgnoreCase("motorista")
                && !usuarioRepository.existsByEmailIgnoreCase("motorista@checklist.com.br")) {
            Usuario motorista = new Usuario(
                    DEFAULT_MOTORISTA_ID,
                    "João Silva (Motorista Rodoviário)",
                    "motorista",
                    "motorista@checklist.com.br",
                    passwordEncoder.encode("senha123"),
                    PerfilUsuario.MOTORISTA,
                    true
            );
            usuarioRepository.save(motorista);
        }
    }
}
