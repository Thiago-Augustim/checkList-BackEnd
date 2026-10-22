package br.com.checklist.repository;

import br.com.checklist.domain.entity.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, UUID> {

    @Query("SELECT DISTINCT t FROM ChecklistTemplate t LEFT JOIN FETCH t.perguntas WHERE t.ativo = true ORDER BY t.titulo ASC")
    List<ChecklistTemplate> findAllActiveWithQuestions();

    @Query("SELECT DISTINCT t FROM ChecklistTemplate t LEFT JOIN FETCH t.perguntas ORDER BY t.criadoEm DESC")
    List<ChecklistTemplate> findAllWithQuestionsOrderByCriadoEmDesc();
}
