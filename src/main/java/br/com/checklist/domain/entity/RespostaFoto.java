package br.com.checklist.domain.entity;

import br.com.checklist.domain.enums.StatusUploadFoto;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "resposta_fotos")
public class RespostaFoto {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resposta_id", nullable = false)
    private ChecklistResposta resposta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_upload", nullable = false, length = 30)
    private StatusUploadFoto statusUpload = StatusUploadFoto.PENDENTE;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    private Double latitude;

    private Double longitude;

    @Column(name = "capturada_em", nullable = false)
    private OffsetDateTime capturadaEm;

    @Column(name = "enviada_em")
    private OffsetDateTime enviadaEm;

    public RespostaFoto() {
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

    public ChecklistResposta getResposta() {
        return resposta;
    }

    public void setResposta(ChecklistResposta resposta) {
        this.resposta = resposta;
    }

    public StatusUploadFoto getStatusUpload() {
        return statusUpload;
    }

    public void setStatusUpload(StatusUploadFoto statusUpload) {
        this.statusUpload = statusUpload;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
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

    public OffsetDateTime getCapturadaEm() {
        return capturadaEm;
    }

    public void setCapturadaEm(OffsetDateTime capturadaEm) {
        this.capturadaEm = capturadaEm;
    }

    public OffsetDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(OffsetDateTime enviadaEm) {
        this.enviadaEm = enviadaEm;
    }
}
