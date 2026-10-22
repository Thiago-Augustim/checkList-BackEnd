package br.com.checklist.repository;

import br.com.checklist.domain.entity.ChecklistResposta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ChecklistRespostaRepository extends JpaRepository<ChecklistResposta, UUID> {
    boolean existsByPerguntaId(UUID perguntaId);
}
