-- ============================================================================
-- V4__add_tipo_veiculo_to_templates.sql
-- Inclui o tipo CARRO na frota e vincula cada Modelo de Checklist a um TipoVeiculo
-- ============================================================================

-- 1. Atualiza a coluna tipo de veiculos para suportar CARRO além dos tipos de caminhões
ALTER TABLE veiculos ADD COLUMN IF NOT EXISTS tipo_novo VARCHAR(40);
UPDATE veiculos SET tipo_novo = tipo;
ALTER TABLE veiculos DROP COLUMN tipo;
ALTER TABLE veiculos RENAME COLUMN tipo_novo TO tipo;
ALTER TABLE veiculos ALTER COLUMN tipo SET NOT NULL;
ALTER TABLE veiculos DROP CONSTRAINT IF EXISTS chk_veiculos_tipo;
ALTER TABLE veiculos ADD CONSTRAINT chk_veiculos_tipo
    CHECK (tipo IN ('CARRO', 'UTILITARIO', 'TOCO', 'TRUCK', 'CAVALO', 'CARRETA', 'CAMINHONETE', 'CAVALO_MECANICO', 'REBOQUE'));

-- 2. Adiciona a coluna tipo_veiculo em checklist_templates
ALTER TABLE checklist_templates ADD COLUMN IF NOT EXISTS tipo_veiculo VARCHAR(40);
UPDATE checklist_templates SET tipo_veiculo = 'CAVALO' WHERE tipo_veiculo IS NULL;
ALTER TABLE checklist_templates ALTER COLUMN tipo_veiculo SET NOT NULL;
ALTER TABLE checklist_templates DROP CONSTRAINT IF EXISTS chk_templates_tipo_veiculo;
ALTER TABLE checklist_templates ADD CONSTRAINT chk_templates_tipo_veiculo
    CHECK (tipo_veiculo IN ('CARRO', 'UTILITARIO', 'TOCO', 'TRUCK', 'CAVALO', 'CARRETA', 'CAMINHONETE', 'CAVALO_MECANICO', 'REBOQUE'));

CREATE INDEX IF NOT EXISTS idx_templates_tipo_ativo ON checklist_templates(tipo_veiculo, ativo);

-- 3. Insere veículo do tipo CARRO e modelos de checklist específicos por tipo para homologação
INSERT INTO veiculos (id, placa, modelo, tipo, km_atual, ativo, criado_em)
SELECT '550e8400-e29b-41d4-a716-446655440004', 'BRA2E19', 'Fiat Strada Endurance 1.3', 'CARRO', 34500, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM veiculos WHERE id = '550e8400-e29b-41d4-a716-446655440004' OR placa = 'BRA2E19');

INSERT INTO checklist_templates (id, titulo, descricao, tipo_veiculo, ativo, criado_em, atualizado_em)
SELECT '770e8400-e29b-41d4-a716-446655440003', 'Vistoria de Carro Leve / Passeio', 'Inspeção de segurança diária para carros leves da frota.', 'CARRO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM checklist_templates WHERE id = '770e8400-e29b-41d4-a716-446655440003');

INSERT INTO checklist_templates (id, titulo, descricao, tipo_veiculo, ativo, criado_em, atualizado_em)
SELECT '770e8400-e29b-41d4-a716-446655440004', 'Vistoria Caminhão Truck (6x2)', 'Inspeção específica de saída para caminhões rígidos tipo Truck.', 'TRUCK', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM checklist_templates WHERE id = '770e8400-e29b-41d4-a716-446655440004');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440009', '770e8400-e29b-41d4-a716-446655440003', 'Pneus, estepe, macaco e chave de roda estão em perfeitas condições?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440009');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440010', '770e8400-e29b-41d4-a716-446655440003', 'Nível de óleo do motor e líquido de arrefecimento estão adequados?', 'BOOLEANO', 2, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440010');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440011', '770e8400-e29b-41d4-a716-446655440003', 'Avalie o estado de conservação da pintura, vidros e retrovisores (1 a 5)', 'ESCALA_1_5', 3, TRUE, TRUE, '2', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440011');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440012', '770e8400-e29b-41d4-a716-446655440004', 'Suspensor do 3º eixo (Truck) e balanças estão funcionando sem folgas?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440012');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440013', '770e8400-e29b-41d4-a716-446655440004', 'Carroceria/baú e travas de segurança da carga estão íntegros?', 'BOOLEANO', 2, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440013');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440014', '770e8400-e29b-41d4-a716-446655440004', 'Avalie o estado geral dos pneus do eixo duplo traseiro (1 a 5)', 'ESCALA_1_5', 3, TRUE, TRUE, '2', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440014');
