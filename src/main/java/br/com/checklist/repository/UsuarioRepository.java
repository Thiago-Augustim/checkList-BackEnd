package br.com.checklist.repository;

import br.com.checklist.domain.entity.Usuario;
import br.com.checklist.domain.enums.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<Usuario> findByPerfilOrderByNomeAsc(PerfilUsuario perfil);
    List<Usuario> findAllByOrderByNomeAsc();
}
