-- ============================================================================
-- V5__update_tipos_veiculo.sql
-- Atualiza as tipagens de veículos e templates para:
-- CARRO, CAMINHONETE, TOCO, TRUCK, CAVALO_MECANICO, REBOQUE
-- ============================================================================

-- 1. Remove as constraints antigas criadas na V4
ALTER TABLE veiculos DROP CONSTRAINT chk_veiculos_tipo;
ALTER TABLE checklist_templates DROP CONSTRAINT chk_templates_tipo_veiculo;

-- 2. Converte os registros existentes na tabela veiculos para a nova nomenclatura
UPDATE veiculos SET tipo = 'CAVALO_MECANICO' WHERE tipo IN ('CAVALO', 'CAVALO-MECANICO');
UPDATE veiculos SET tipo = 'REBOQUE' WHERE tipo = 'CARRETA';
UPDATE veiculos SET tipo = 'CAMINHONETE' WHERE tipo = 'UTILITARIO';

-- 3. Converte os registros existentes na tabela checklist_templates para a nova nomenclatura
UPDATE checklist_templates SET tipo_veiculo = 'CAVALO_MECANICO' WHERE tipo_veiculo IN ('CAVALO', 'CAVALO-MECANICO');
UPDATE checklist_templates SET tipo_veiculo = 'REBOQUE' WHERE tipo_veiculo = 'CARRETA';
UPDATE checklist_templates SET tipo_veiculo = 'CAMINHONETE' WHERE tipo_veiculo = 'UTILITARIO';

-- 4. Recria as constraints com os novos valores permitidos
ALTER TABLE veiculos ADD CONSTRAINT chk_veiculos_tipo
    CHECK (tipo IN ('CARRO', 'CAMINHONETE', 'TOCO', 'TRUCK', 'CAVALO_MECANICO', 'REBOQUE'));

ALTER TABLE checklist_templates ADD CONSTRAINT chk_templates_tipo_veiculo
    CHECK (tipo_veiculo IN ('CARRO', 'CAMINHONETE', 'TOCO', 'TRUCK', 'CAVALO_MECANICO', 'REBOQUE'));
