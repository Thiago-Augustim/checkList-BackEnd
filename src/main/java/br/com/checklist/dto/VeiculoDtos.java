package br.com.checklist.dto;

import br.com.checklist.domain.enums.TipoVeiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class VeiculoDtos {

    private VeiculoDtos() {
    }

    public record VeiculoDto(
            UUID id,
            String placa,
            String modelo,
            TipoVeiculo tipo,
            Integer kmAtual,
            boolean ativo,
            OffsetDateTime criadoEm
    ) {
    }

    public record SaveVeiculoRequest(
            @NotBlank String placa,
            @NotBlank String modelo,
            @NotNull TipoVeiculo tipo,
            @NotNull @Min(0) Integer kmAtual
    ) {
    }
}
