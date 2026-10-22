package br.com.checklist.repository;

import br.com.checklist.domain.entity.Checklist;
import br.com.checklist.domain.enums.StatusSincronizacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChecklistRepository extends JpaRepository<Checklist, UUID> {

    @Query("""
        SELECT c FROM Checklist c
        JOIN FETCH c.veiculo
        JOIN FETCH c.motorista
        JOIN FETCH c.template
        ORDER BY c.realizadoEm DESC
    """)
    List<Checklist> findAllWithSummaryOrderByRealizadoEmDesc();

    @Query("""
        SELECT DISTINCT c FROM Checklist c
        JOIN FETCH c.veiculo
        JOIN FETCH c.motorista
        JOIN FETCH c.template
        LEFT JOIN FETCH c.respostas r
        LEFT JOIN FETCH r.pergunta
        WHERE c.id = :id
    """)
    Optional<Checklist> findByIdWithDetails(@Param("id") UUID id);

    long countByPossuiAvariaTrue();
    long countByStatusSincronizacao(StatusSincronizacao status);
}
