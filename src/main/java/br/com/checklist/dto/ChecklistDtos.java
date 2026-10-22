package br.com.checklist.dto;

import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.domain.enums.StatusUploadFoto;
import br.com.checklist.domain.enums.TipoResposta;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ChecklistDtos {

    private ChecklistDtos() {
    }

    public record FotoDetalheDto(
            UUID id,
            StatusUploadFoto statusUpload,
            String urlArquivo,
            Long tamanhoBytes,
            Double latitude,
            Double longitude,
            OffsetDateTime capturadaEm,
            OffsetDateTime enviadaEm
    ) {
    }

    public record RespostaDetalheDto(
            UUID id,
            UUID perguntaId,
            String enunciadoPergunta,
            TipoResposta tipoResposta,
            Integer ordemPergunta,
            String valorResposta,
            String observacaoAvaria,
            boolean avariaDetectada,
            List<FotoDetalheDto> fotos
    ) {
    }

    public record ChecklistResumoDto(
            UUID id,
            UUID templateId,
            String templateTitulo,
            UUID veiculoId,
            String veiculoPlaca,
            String veiculoModelo,
            UUID motoristaId,
            String motoristaNome,
            Integer kmVistoria,
            StatusSincronizacao statusSincronizacao,
            boolean possuiAvaria,
            boolean alertaInconsistencia,
            String motivoInconsistencia,
            OffsetDateTime realizadoEm,
            OffsetDateTime sincronizadoEm
    ) {
    }

    public record ChecklistDetalheDto(
            UUID id,
            UUID templateId,
            String templateTitulo,
            UUID veiculoId,
            String veiculoPlaca,
            String veiculoModelo,
            UUID motoristaId,
            String motoristaNome,
            Integer kmVistoria,
            String observacoesGerais,
            Double latitude,
            Double longitude,
            StatusSincronizacao statusSincronizacao,
            boolean possuiAvaria,
            boolean alertaInconsistencia,
            String motivoInconsistencia,
            OffsetDateTime realizadoEm,
            OffsetDateTime sincronizadoEm,
            List<RespostaDetalheDto> respostas
    ) {
    }

    public record DashboardResumoDto(
            long totalChecklists,
            long checklistsComAvaria,
            long checklistsAguardandoFotos,
            long totalVeiculosAtivos,
            long totalTemplatesAtivos
    ) {
    }
}
