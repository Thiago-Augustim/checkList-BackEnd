package br.com.checklist.dto;

import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.domain.enums.StatusUploadFoto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SyncDtos {

    private SyncDtos() {
    }

    public record CatalogoSyncResponse(
            OffsetDateTime sincronizadoEm,
            List<VeiculoDtos.VeiculoDto> veiculos,
            List<TemplateDtos.TemplateDto> templates
    ) {
    }

    public record SyncFotoEsperadaRequest(
            @NotNull UUID id,
            @NotNull OffsetDateTime capturadaEm,
            Double latitude,
            Double longitude
    ) {
    }

    public record SyncRespostaRequest(
            @NotNull UUID id,
            @NotNull UUID perguntaId,
            @NotBlank String valorResposta,
            String observacaoAvaria,
            boolean avariaDetectada,
            @Valid List<SyncFotoEsperadaRequest> fotosEsperadas
    ) {
    }

    public record SyncChecklistRequest(
            @NotNull UUID id,
            @NotNull UUID templateId,
            @NotNull UUID veiculoId,
            @NotNull @Min(0) Integer kmVistoria,
            String observacoesGerais,
            Double latitude,
            Double longitude,
            @NotNull OffsetDateTime realizadoEm,
            @NotEmpty @Valid List<SyncRespostaRequest> respostas
    ) {
    }

    public record SyncChecklistResponse(
            UUID checklistId,
            StatusSincronizacao statusSincronizacao,
            boolean alertaInconsistencia,
            String motivoInconsistencia,
            List<UUID> fotosPendentesIds
    ) {
    }

    public record FotoUploadResponse(
            UUID fotoId,
            UUID checklistId,
            StatusUploadFoto statusUpload,
            StatusSincronizacao statusChecklist,
            String urlArquivo
    ) {
    }
}
