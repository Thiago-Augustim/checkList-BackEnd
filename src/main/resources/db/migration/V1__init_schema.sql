-- ============================================================================
-- V1__init_schema.sql
-- Modelagem 100% Relacional Estrita para o Sistema CheckList (Offline-First)
-- ============================================================================

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil VARCHAR(30) NOT NULL CHECK (perfil IN ('GESTOR', 'MOTORISTA')),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_perfil_ativo ON usuarios(perfil, ativo);

CREATE TABLE veiculos (
    id UUID PRIMARY KEY,
    placa VARCHAR(10) NOT NULL UNIQUE,
    modelo VARCHAR(120) NOT NULL,
    tipo VARCHAR(40) NOT NULL CHECK (tipo IN ('CAVALO', 'CARRETA', 'TRUCK', 'TOCO', 'UTILITARIO')),
    km_atual INTEGER NOT NULL DEFAULT 0 CHECK (km_atual >= 0),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_veiculos_ativo ON veiculos(ativo);

CREATE TABLE checklist_templates (
    id UUID PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_templates_ativo ON checklist_templates(ativo);

CREATE TABLE template_perguntas (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES checklist_templates(id),
    enunciado VARCHAR(500) NOT NULL,
    tipo_resposta VARCHAR(30) NOT NULL CHECK (tipo_resposta IN ('TEXTO', 'BOOLEANO', 'ESCALA_1_5')),
    ordem INTEGER NOT NULL DEFAULT 1,
    obrigatoria BOOLEAN NOT NULL DEFAULT TRUE,
    exige_foto_se_avaria BOOLEAN NOT NULL DEFAULT FALSE,
    valor_gatilho_avaria VARCHAR(50),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_perguntas_template_ativo ON template_perguntas(template_id, ativo, ordem);

CREATE TABLE checklists (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES checklist_templates(id),
    veiculo_id UUID NOT NULL REFERENCES veiculos(id),
    motorista_id UUID NOT NULL REFERENCES usuarios(id),
    km_vistoria INTEGER NOT NULL CHECK (km_vistoria >= 0),
    observacoes_gerais VARCHAR(1000),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    status_sincronizacao VARCHAR(30) NOT NULL CHECK (status_sincronizacao IN ('AGUARDANDO_FOTOS', 'CONCLUIDO')),
    possui_avaria BOOLEAN NOT NULL DEFAULT FALSE,
    alerta_inconsistencia BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_inconsistencia VARCHAR(500),
    realizado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    sincronizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_checklists_veiculo ON checklists(veiculo_id);
CREATE INDEX idx_checklists_motorista ON checklists(motorista_id);
CREATE INDEX idx_checklists_status ON checklists(status_sincronizacao);
CREATE INDEX idx_checklists_realizado_em ON checklists(realizado_em DESC);

CREATE TABLE checklist_respostas (
    id UUID PRIMARY KEY,
    checklist_id UUID NOT NULL REFERENCES checklists(id) ON DELETE CASCADE,
    pergunta_id UUID NOT NULL REFERENCES template_perguntas(id),
    valor_resposta VARCHAR(2000) NOT NULL,
    observacao_avaria VARCHAR(1000),
    avaria_detectada BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_checklist_pergunta UNIQUE (checklist_id, pergunta_id)
);

CREATE INDEX idx_respostas_checklist ON checklist_respostas(checklist_id);

CREATE TABLE resposta_fotos (
    id UUID PRIMARY KEY,
    resposta_id UUID NOT NULL REFERENCES checklist_respostas(id) ON DELETE CASCADE,
    status_upload VARCHAR(30) NOT NULL CHECK (status_upload IN ('PENDENTE', 'CONCLUIDO')),
    storage_key VARCHAR(500),
    mime_type VARCHAR(100),
    tamanho_bytes BIGINT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    capturada_em TIMESTAMP WITH TIME ZONE NOT NULL,
    enviada_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_fotos_resposta ON resposta_fotos(resposta_id);
CREATE INDEX idx_fotos_status ON resposta_fotos(status_upload);
