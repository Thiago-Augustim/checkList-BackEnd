package br.com.checklist.domain.entity;

import br.com.checklist.domain.enums.StatusSincronizacao;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checklists")
public class Checklist {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motorista_id", nullable = false)
    private Usuario motorista;

    @Column(name = "km_vistoria", nullable = false)
    private Integer kmVistoria;

    @Column(name = "observacoes_gerais", length = 1000)
    private String observacoesGerais;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_sincronizacao", nullable = false, length = 30)
    private StatusSincronizacao statusSincronizacao;

    @Column(name = "possui_avaria", nullable = false)
    private boolean possuiAvaria = false;

    @Column(name = "alerta_inconsistencia", nullable = false)
    private boolean alertaInconsistencia = false;

    @Column(name = "motivo_inconsistencia", length = 500)
    private String motivoInconsistencia;

    @Column(name = "realizado_em", nullable = false)
    private OffsetDateTime realizadoEm;

    @Column(name = "sincronizado_em", nullable = false)
    private OffsetDateTime sincronizadoEm = OffsetDateTime.now();

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChecklistResposta> respostas = new ArrayList<>();

    public Checklist() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.sincronizadoEm == null) {
            this.sincronizadoEm = OffsetDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ChecklistTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ChecklistTemplate template) {
        this.template = template;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Integer getKmVistoria() {
        return kmVistoria;
    }

    public void setKmVistoria(Integer kmVistoria) {
        this.kmVistoria = kmVistoria;
    }

    public String getObservacoesGerais() {
        return observacoesGerais;
    }

    public void setObservacoesGerais(String observacoesGerais) {
        this.observacoesGerais = observacoesGerais;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public StatusSincronizacao getStatusSincronizacao() {
        return statusSincronizacao;
    }

    public void setStatusSincronizacao(StatusSincronizacao statusSincronizacao) {
        this.statusSincronizacao = statusSincronizacao;
    }

    public boolean isPossuiAvaria() {
        return possuiAvaria;
    }

    public void setPossuiAvaria(boolean possuiAvaria) {
        this.possuiAvaria = possuiAvaria;
    }

    public boolean isAlertaInconsistencia() {
        return alertaInconsistencia;
    }

    public void setAlertaInconsistencia(boolean alertaInconsistencia) {
        this.alertaInconsistencia = alertaInconsistencia;
    }

    public String getMotivoInconsistencia() {
        return motivoInconsistencia;
    }

    public void setMotivoInconsistencia(String motivoInconsistencia) {
        this.motivoInconsistencia = motivoInconsistencia;
    }

    public OffsetDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public void setRealizadoEm(OffsetDateTime realizadoEm) {
        this.realizadoEm = realizadoEm;
    }

    public OffsetDateTime getSincronizadoEm() {
        return sincronizadoEm;
    }

    public void setSincronizadoEm(OffsetDateTime sincronizadoEm) {
        this.sincronizadoEm = sincronizadoEm;
    }

    public List<ChecklistResposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<ChecklistResposta> respostas) {
        this.respostas = respostas;
    }
}
