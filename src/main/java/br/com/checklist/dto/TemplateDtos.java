package br.com.checklist.dto;

import br.com.checklist.domain.enums.TipoResposta;
import br.com.checklist.domain.enums.TipoVeiculo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class TemplateDtos {

    private TemplateDtos() {
    }

    public record PerguntaDto(
            UUID id,
            String enunciado,
            TipoResposta tipoResposta,
            Integer ordem,
            boolean obrigatoria,
            boolean exigeFotoSeAvaria,
            String valorGatilhoAvaria,
            boolean ativo
    ) {
    }

    public record SavePerguntaRequest(
            UUID id,
            @NotBlank String enunciado,
            @NotNull TipoResposta tipoResposta,
            @NotNull Integer ordem,
            boolean obrigatoria,
            boolean exigeFotoSeAvaria,
            String valorGatilhoAvaria
    ) {
    }

    public record TemplateDto(
            UUID id,
            String titulo,
            String descricao,
            TipoVeiculo tipoVeiculo,
            boolean ativo,
            OffsetDateTime criadoEm,
            OffsetDateTime atualizadoEm,
            List<PerguntaDto> perguntas
    ) {
    }

    public record SaveTemplateRequest(
            @NotBlank String titulo,
            String descricao,
            TipoVeiculo tipoVeiculo,
            @NotEmpty @Valid List<SavePerguntaRequest> perguntas
    ) {
    }
}
