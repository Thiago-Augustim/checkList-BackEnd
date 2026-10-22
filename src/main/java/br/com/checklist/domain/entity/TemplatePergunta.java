package br.com.checklist.domain.entity;

import br.com.checklist.domain.enums.TipoResposta;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "template_perguntas")
public class TemplatePergunta {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private ChecklistTemplate template;

    @Column(nullable = false, length = 500)
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_resposta", nullable = false, length = 30)
    private TipoResposta tipoResposta;

    @Column(nullable = false)
    private Integer ordem = 1;

    @Column(nullable = false)
    private boolean obrigatoria = true;

    @Column(name = "exige_foto_se_avaria", nullable = false)
    private boolean exigeFotoSeAvaria = false;

    @Column(name = "valor_gatilho_avaria", length = 50)
    private String valorGatilhoAvaria;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    public TemplatePergunta() {
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

    public ChecklistTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ChecklistTemplate template) {
        this.template = template;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public TipoResposta getTipoResposta() {
        return tipoResposta;
    }

    public void setTipoResposta(TipoResposta tipoResposta) {
        this.tipoResposta = tipoResposta;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public boolean isExigeFotoSeAvaria() {
        return exigeFotoSeAvaria;
    }

    public void setExigeFotoSeAvaria(boolean exigeFotoSeAvaria) {
        this.exigeFotoSeAvaria = exigeFotoSeAvaria;
    }

    public String getValorGatilhoAvaria() {
        return valorGatilhoAvaria;
    }

    public void setValorGatilhoAvaria(String valorGatilhoAvaria) {
        this.valorGatilhoAvaria = valorGatilhoAvaria;
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
