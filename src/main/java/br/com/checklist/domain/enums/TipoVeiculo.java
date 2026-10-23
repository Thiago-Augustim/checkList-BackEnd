package br.com.checklist.domain.enums;

import com.fasterxml.jackson.annotation.JsonAlias;

public enum TipoVeiculo {
    CARRO,
    @JsonAlias("UTILITARIO")
    CAMINHONETE,
    TOCO,
    TRUCK,
    @JsonAlias({"CAVALO-MECANICO", "CAVALO"})
    CAVALO_MECANICO,
    @JsonAlias("CARRETA")
    REBOQUE
}
