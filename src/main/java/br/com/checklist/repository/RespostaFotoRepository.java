package br.com.checklist.repository;

import br.com.checklist.domain.entity.RespostaFoto;
import br.com.checklist.domain.enums.StatusUploadFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface RespostaFotoRepository extends JpaRepository<RespostaFoto, UUID> {

    @Query("""
        SELECT f FROM RespostaFoto f
        WHERE f.resposta.checklist.id = :checklistId
          AND f.statusUpload = :status
    """)
    List<RespostaFoto> findByChecklistIdAndStatusUpload(
            @Param("checklistId") UUID checklistId,
            @Param("status") StatusUploadFoto status
    );

    @Query("""
        SELECT f FROM RespostaFoto f
        WHERE f.resposta.checklist.id = :checklistId
    """)
    List<RespostaFoto> findAllByChecklistId(@Param("checklistId") UUID checklistId);
}
