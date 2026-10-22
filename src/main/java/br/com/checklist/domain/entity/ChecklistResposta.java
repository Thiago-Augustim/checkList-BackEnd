package br.com.checklist.domain.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "checklist_respostas")
public class ChecklistResposta {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private TemplatePergunta pergunta;

    @Column(name = "valor_resposta", nullable = false, length = 2000)
    private String valorResposta;

    @Column(name = "observacao_avaria", length = 1000)
    private String observacaoAvaria;

    @Column(name = "avaria_detectada", nullable = false)
    private boolean avariaDetectada = false;

    @OneToMany(mappedBy = "resposta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespostaFoto> fotos = new ArrayList<>();

    public ChecklistResposta() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public TemplatePergunta getPergunta() {
        return pergunta;
    }

    public void setPergunta(TemplatePergunta pergunta) {
        this.pergunta = pergunta;
    }

    public String getValorResposta() {
        return valorResposta;
    }

    public void setValorResposta(String valorResposta) {
        this.valorResposta = valorResposta;
    }

    public String getObservacaoAvaria() {
        return observacaoAvaria;
    }

    public void setObservacaoAvaria(String observacaoAvaria) {
        this.observacaoAvaria = observacaoAvaria;
    }

    public boolean isAvariaDetectada() {
        return avariaDetectada;
    }

    public void setAvariaDetectada(boolean avariaDetectada) {
        this.avariaDetectada = avariaDetectada;
    }

    public List<RespostaFoto> getFotos() {
        return fotos;
    }

    public void setFotos(List<RespostaFoto> fotos) {
        this.fotos = fotos;
    }
}
