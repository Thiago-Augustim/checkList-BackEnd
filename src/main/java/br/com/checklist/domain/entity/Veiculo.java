package br.com.checklist.domain.entity;

import br.com.checklist.domain.enums.TipoVeiculo;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 120)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoVeiculo tipo;

    @Column(name = "km_atual", nullable = false)
    private Integer kmAtual = 0;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public Veiculo() {
    }

    public Veiculo(UUID id, String placa, String modelo, TipoVeiculo tipo, Integer kmAtual, boolean ativo) {
        this.id = id;
        this.placa = placa;
        this.modelo = modelo;
        this.tipo = tipo;
        this.kmAtual = kmAtual;
        this.ativo = ativo;
        this.criadoEm = OffsetDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculo tipo) {
        this.tipo = tipo;
    }

    public Integer getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(Integer kmAtual) {
        this.kmAtual = kmAtual;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
