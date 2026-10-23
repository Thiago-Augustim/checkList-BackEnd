-- ============================================================================
-- V2__seed_initial_data.sql
-- Dados iniciais de Frota e Modelos de Checklist para homologação e testes
-- ============================================================================

ALTER TABLE veiculos DROP CONSTRAINT IF EXISTS chk_veiculos_tipo;
ALTER TABLE checklist_templates DROP CONSTRAINT IF EXISTS chk_templates_tipo_veiculo;
ALTER TABLE checklist_templates ADD COLUMN IF NOT EXISTS tipo_veiculo VARCHAR(40) DEFAULT 'CAVALO_MECANICO';
ALTER TABLE checklist_templates ALTER COLUMN tipo_veiculo SET DEFAULT 'CAVALO_MECANICO';

INSERT INTO veiculos (id, placa, modelo, tipo, km_atual, ativo, criado_em)
SELECT '550e8400-e29b-41d4-a716-446655440001', 'ABC1D23', 'Scania R450 6x2', 'CAVALO_MECANICO', 145200, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM veiculos WHERE id = '550e8400-e29b-41d4-a716-446655440001' OR placa = 'ABC1D23');

INSERT INTO veiculos (id, placa, modelo, tipo, km_atual, ativo, criado_em)
SELECT '550e8400-e29b-41d4-a716-446655440002', 'XYZ9F87', 'Volvo FH 540 6x4', 'CAVALO_MECANICO', 89300, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM veiculos WHERE id = '550e8400-e29b-41d4-a716-446655440002' OR placa = 'XYZ9F87');

INSERT INTO veiculos (id, placa, modelo, tipo, km_atual, ativo, criado_em)
SELECT '550e8400-e29b-41d4-a716-446655440003', 'QWE4R56', 'Mercedes-Benz Actros 2651', 'TRUCK', 210450, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM veiculos WHERE id = '550e8400-e29b-41d4-a716-446655440003' OR placa = 'QWE4R56');

INSERT INTO checklist_templates (id, titulo, descricao, ativo, criado_em, atualizado_em)
SELECT '770e8400-e29b-41d4-a716-446655440001', 'Vistoria Pré-Viagem (Saída de Pátio)', 'Inspeção obrigatória de segurança antes do início de qualquer viagem rodoviária.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM checklist_templates WHERE id = '770e8400-e29b-41d4-a716-446655440001');

INSERT INTO checklist_templates (id, titulo, descricao, ativo, criado_em, atualizado_em)
SELECT '770e8400-e29b-41d4-a716-446655440002', 'Vistoria de Retorno (Chegada)', 'Conferência do estado geral do caminhão após encerramento de rota.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM checklist_templates WHERE id = '770e8400-e29b-41d4-a716-446655440002');

-- Perguntas do Template 1: Vistoria Pré-Viagem
INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'Os pneus dianteiros e traseiros estão calibrados e sem cortes ou bolhas?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440001');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 'O sistema de freios e mangueiras de ar estão sem vazamentos?', 'BOOLEANO', 2, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440002');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'Faróis, lanternas, setas e luz de freio estão funcionando perfeitamente?', 'BOOLEANO', 3, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440003');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', 'Avalie o estado geral da lataria e para-choques (1 = Crítico, 5 = Excelente)', 'ESCALA_1_5', 4, TRUE, TRUE, '2', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440004');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440001', 'Informe o destino da viagem ou observação adicional do painel:', 'TEXTO', 5, FALSE, FALSE, NULL, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440005');

-- Perguntas do Template 2: Vistoria de Retorno
INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440002', 'O veículo retornou sem novas avarias externas na carroceria/cabine?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440006');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440007', '770e8400-e29b-41d4-a716-446655440002', 'Avalie o nível de limpeza interna da cabine na entrega (1 a 5)', 'ESCALA_1_5', 2, TRUE, FALSE, '2', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440007');

INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em)
SELECT '990e8400-e29b-41d4-a716-446655440008', '770e8400-e29b-41d4-a716-446655440002', 'Relate qualquer ruído ou comportamento anormal percebido na viagem:', 'TEXTO', 3, FALSE, FALSE, NULL, TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM template_perguntas WHERE id = '990e8400-e29b-41d4-a716-446655440008');
