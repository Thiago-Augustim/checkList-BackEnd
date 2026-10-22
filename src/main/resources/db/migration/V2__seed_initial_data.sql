-- ============================================================================
-- V2__seed_initial_data.sql
-- Dados iniciais de Frota e Modelos de Checklist para homologação e testes
-- ============================================================================

INSERT INTO veiculos (id, placa, modelo, tipo, km_atual, ativo, criado_em) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'ABC1D23', 'Scania R450 6x2', 'CAVALO', 145200, TRUE, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'XYZ9F87', 'Volvo FH 540 6x4', 'CAVALO', 89300, TRUE, CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440003', 'QWE4R56', 'Mercedes-Benz Actros 2651', 'TRUCK', 210450, TRUE, CURRENT_TIMESTAMP);

INSERT INTO checklist_templates (id, titulo, descricao, ativo, criado_em, atualizado_em) VALUES
    ('770e8400-e29b-41d4-a716-446655440001', 'Vistoria Pré-Viagem (Saída de Pátio)', 'Inspeção obrigatória de segurança antes do início de qualquer viagem rodoviária.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('770e8400-e29b-41d4-a716-446655440002', 'Vistoria de Retorno (Chegada)', 'Conferência do estado geral do caminhão após encerramento de rota.', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Perguntas do Template 1: Vistoria Pré-Viagem
INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em) VALUES
    ('990e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', 'Os pneus dianteiros e traseiros estão calibrados e sem cortes ou bolhas?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440001', 'O sistema de freios e mangueiras de ar estão sem vazamentos?', 'BOOLEANO', 2, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440001', 'Faróis, lanternas, setas e luz de freio estão funcionando perfeitamente?', 'BOOLEANO', 3, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', 'Avalie o estado geral da lataria e para-choques (1 = Crítico, 5 = Excelente)', 'ESCALA_1_5', 4, TRUE, TRUE, '2', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440001', 'Informe o destino da viagem ou observação adicional do painel:', 'TEXTO', 5, FALSE, FALSE, NULL, TRUE, CURRENT_TIMESTAMP);

-- Perguntas do Template 2: Vistoria de Retorno
INSERT INTO template_perguntas (id, template_id, enunciado, tipo_resposta, ordem, obrigatoria, exige_foto_se_avaria, valor_gatilho_avaria, ativo, criado_em) VALUES
    ('990e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440002', 'O veículo retornou sem novas avarias externas na carroceria/cabine?', 'BOOLEANO', 1, TRUE, TRUE, 'FALSE', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440007', '770e8400-e29b-41d4-a716-446655440002', 'Avalie o nível de limpeza interna da cabine na entrega (1 a 5)', 'ESCALA_1_5', 2, TRUE, FALSE, '2', TRUE, CURRENT_TIMESTAMP),
    ('990e8400-e29b-41d4-a716-446655440008', '770e8400-e29b-41d4-a716-446655440002', 'Relate qualquer ruído ou comportamento anormal percebido na viagem:', 'TEXTO', 3, FALSE, FALSE, NULL, TRUE, CURRENT_TIMESTAMP);
