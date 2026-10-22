package br.com.checklist.repository;

import br.com.checklist.domain.entity.TemplatePergunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TemplatePerguntaRepository extends JpaRepository<TemplatePergunta, UUID> {
    List<TemplatePergunta> findByTemplateIdAndAtivoTrueOrderByOrdemAsc(UUID templateId);
    List<TemplatePergunta> findByTemplateIdOrderByOrdemAsc(UUID templateId);
}
