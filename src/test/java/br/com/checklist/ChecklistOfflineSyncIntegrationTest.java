package br.com.checklist;

import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.domain.enums.StatusUploadFoto;
import br.com.checklist.domain.enums.TipoResposta;
import br.com.checklist.domain.enums.TipoVeiculo;
import br.com.checklist.dto.AuthDtos.*;
import br.com.checklist.dto.ChecklistDtos.ChecklistDetalheDto;
import br.com.checklist.dto.SyncDtos.*;
import br.com.checklist.dto.TemplateDtos.*;
import br.com.checklist.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class ChecklistOfflineSyncIntegrationTest {

    @Autowired
    private AuthUsuarioService authUsuarioService;

    @Autowired
    private VeiculoService veiculoService;

    @Autowired
    private ChecklistTemplateService templateService;

    @Autowired
    private ChecklistSyncService syncService;

    @Autowired
    private ChecklistConsultaService consultaService;

    @Test
    @DisplayName("Deve autenticar motorista, baixar catálogo e sincronizar checklist offline em 2 etapas com idempotência")
    void deveExecutarFluxoCompletoOfflineSyncEmDuasEtapasComIdempotencia() {
        // 1. Autenticação do motorista
        AuthResponse auth = authUsuarioService.autenticar(
                new LoginRequest("motorista", "senha123")
        );
        assertThat(auth.accessToken()).isNotBlank();
        assertThat(auth.refreshToken()).isNotBlank();
        UUID motoristaId = auth.usuario().id();

        // 2. Downsync do catálogo ativo
        CatalogoSyncResponse catalogo = syncService.obterCatalogoAtivo();
        assertThat(catalogo.veiculos()).isNotEmpty();
        assertThat(catalogo.templates()).isNotEmpty();

        UUID veiculoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID templateId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        UUID perguntaPneuId = UUID.fromString("990e8400-e29b-41d4-a716-446655440001");
        UUID perguntaEscalaId = UUID.fromString("990e8400-e29b-41d4-a716-446655440004");

        // 3. Etapa 1 do Upsync: Envio do JSON leve com avaria apontada e 1 foto esperada
        UUID checklistId = UUID.randomUUID();
        UUID respostaPneuId = UUID.randomUUID();
        UUID respostaEscalaId = UUID.randomUUID();
        UUID fotoPneuId = UUID.randomUUID();

        SyncChecklistRequest syncRequest = new SyncChecklistRequest(
                checklistId,
                templateId,
                veiculoId,
                146000,
                "Vistoria preenchida offline na rodovia BR-116",
                -23.5505,
                -46.6333,
                OffsetDateTime.now().minusHours(3),
                List.of(
                        new SyncRespostaRequest(
                                respostaPneuId,
                                perguntaPneuId,
                                "FALSE",
                                "Corte lateral no pneu dianteiro direito",
                                true,
                                List.of(new SyncFotoEsperadaRequest(
                                        fotoPneuId,
                                        OffsetDateTime.now().minusHours(3),
                                        -23.5505,
                                        -46.6333
                                ))
                        ),
                        new SyncRespostaRequest(
                                respostaEscalaId,
                                perguntaEscalaId,
                                "4",
                                null,
                                false,
                                List.of()
                        )
                )
        );

        SyncChecklistResponse etapa1Resp = syncService.sincronizarChecklist(motoristaId, syncRequest);
        assertThat(etapa1Resp.checklistId()).isEqualTo(checklistId);
        assertThat(etapa1Resp.statusSincronizacao()).isEqualTo(StatusSincronizacao.AGUARDANDO_FOTOS);
        assertThat(etapa1Resp.fotosPendentesIds()).containsExactly(fotoPneuId);

        // 4. Teste de Idempotência da Etapa 1: reenviar o mesmo JSON não deve duplicar nem dar erro
        SyncChecklistResponse reenvioResp = syncService.sincronizarChecklist(motoristaId, syncRequest);
        assertThat(reenvioResp.checklistId()).isEqualTo(checklistId);
        assertThat(reenvioResp.statusSincronizacao()).isEqualTo(StatusSincronizacao.AGUARDANDO_FOTOS);
        assertThat(reenvioResp.fotosPendentesIds()).containsExactly(fotoPneuId);

        // 5. Etapa 2 do Upsync: Upload Multipart da foto de avaria
        MockMultipartFile arquivoFoto = new MockMultipartFile(
                "arquivo",
                "avaria_pneu.jpg",
                "image/jpeg",
                "conteudo-binario-jpeg-comprimido".getBytes(StandardCharsets.UTF_8)
        );

        FotoUploadResponse uploadResp = syncService.receberUploadFoto(fotoPneuId, arquivoFoto);
        assertThat(uploadResp.statusUpload()).isEqualTo(StatusUploadFoto.CONCLUIDO);
        assertThat(uploadResp.statusChecklist()).isEqualTo(StatusSincronizacao.CONCLUIDO);

        // 6. Consulta detalhada pelo painel do gestor
        ChecklistDetalheDto detalhe = consultaService.buscarDetalhePorId(checklistId);
        assertThat(detalhe.statusSincronizacao()).isEqualTo(StatusSincronizacao.CONCLUIDO);
        assertThat(detalhe.possuiAvaria()).isTrue();
        assertThat(detalhe.respostas()).hasSize(2);
    }

    @Test
    @DisplayName("Deve preservar histórico via Soft Delete ao editar pergunta já respondida e aceitar sync de veículo inativado")
    void devePreservarHistoricoAoEditarPerguntaEAceitarSyncDeVeiculoInativado() {
        AuthResponse auth = authUsuarioService.autenticar(
                new LoginRequest("motorista", "senha123")
        );
        UUID motoristaId = auth.usuario().id();
        UUID veiculoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        UUID templateId = UUID.fromString("770e8400-e29b-41d4-a716-446655440002");
        UUID perguntaRetornoId = UUID.fromString("990e8400-e29b-41d4-a716-446655440006");

        // Inativa o veículo enquanto o motorista estava na estrada sem sinal
        veiculoService.alterarStatusAtivo(veiculoId, false);

        // Motorista envia vistoria sem avarias
        UUID checklistId = UUID.randomUUID();
        SyncChecklistRequest syncRequest = new SyncChecklistRequest(
                checklistId,
                templateId,
                veiculoId,
                90100,
                "Retorno sem avarias",
                null,
                null,
                OffsetDateTime.now().minusHours(1),
                List.of(
                        new SyncRespostaRequest(
                                UUID.randomUUID(),
                                perguntaRetornoId,
                                "TRUE",
                                null,
                                false,
                                List.of()
                        )
                )
        );

        SyncChecklistResponse resp = syncService.sincronizarChecklist(motoristaId, syncRequest);
        // Como não há fotos pendentes, já nasce CONCLUIDO e marca alertaInconsistencia = true (veículo inativo)
        assertThat(resp.statusSincronizacao()).isEqualTo(StatusSincronizacao.CONCLUIDO);
        assertThat(resp.alertaInconsistencia()).isTrue();
        assertThat(resp.motivoInconsistencia()).contains("inativado");

        // Agora o gestor edita o enunciado da pergunta que já possui resposta
        TemplateDto templateAtualizado = templateService.atualizar(
                templateId,
                new SaveTemplateRequest(
                        "Vistoria de Retorno Atualizada",
                        "Descrição atualizada",
                        TipoVeiculo.CAVALO,
                        List.of(
                                new SavePerguntaRequest(
                                        perguntaRetornoId,
                                        "Enunciado alterado pelo gestor após vistorias",
                                        TipoResposta.BOOLEANO,
                                        1,
                                        true,
                                        true,
                                        "FALSE"
                                )
                        )
                )
        );

        // A pergunta ativa agora tem um novo UUID, e a antiga sofreu Soft Delete mantendo o histórico intacto!
        assertThat(templateAtualizado.perguntas()).hasSize(1);
        assertThat(templateAtualizado.perguntas().getFirst().id()).isNotEqualTo(perguntaRetornoId);

        ChecklistDetalheDto vistoriaAntiga = consultaService.buscarDetalhePorId(checklistId);
        assertThat(vistoriaAntiga.respostas().getFirst().perguntaId()).isEqualTo(perguntaRetornoId);
    }

    @Test
    @DisplayName("Deve validar conversão em tempo de execução dos tipos dinâmicos (BOOLEANO e ESCALA_1_5) e exigir foto quando avaria detectada")
    void deveValidarRegrasDeTiposDinamicosEFotoObrigatoriaEmCasoDeAvaria() {
        AuthResponse auth = authUsuarioService.autenticar(
                new LoginRequest("motorista", "senha123")
        );
        UUID motoristaId = auth.usuario().id();
        UUID veiculoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        UUID templateId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        UUID perguntaPneuBooleanaId = UUID.fromString("990e8400-e29b-41d4-a716-446655440001");
        UUID perguntaEscalaId = UUID.fromString("990e8400-e29b-41d4-a716-446655440004");

        // 1. Tenta enviar avaria em pergunta que exige foto sem anexar foto esperada -> Deve lançar BusinessValidationException
        SyncChecklistRequest semFotoNaAvaria = new SyncChecklistRequest(
                UUID.randomUUID(),
                templateId,
                veiculoId,
                147000,
                null,
                null,
                null,
                OffsetDateTime.now(),
                List.of(
                        new SyncRespostaRequest(
                                UUID.randomUUID(),
                                perguntaPneuBooleanaId,
                                "FALSE",
                                "Pneu rasgado sem foto",
                                true,
                                List.of()
                        )
                )
        );

        assertThatThrownBy(() -> syncService.sincronizarChecklist(motoristaId, semFotoNaAvaria))
                .hasMessageContaining("exige pelo menos uma foto da avaria");

        // 2. Tenta enviar valor fora da escala 1..5 -> Deve lançar BusinessValidationException
        SyncChecklistRequest escalaInvalida = new SyncChecklistRequest(
                UUID.randomUUID(),
                templateId,
                veiculoId,
                147000,
                null,
                null,
                null,
                OffsetDateTime.now(),
                List.of(
                        new SyncRespostaRequest(
                                UUID.randomUUID(),
                                perguntaEscalaId,
                                "9",
                                null,
                                false,
                                List.of()
                        )
                )
        );

        assertThatThrownBy(() -> syncService.sincronizarChecklist(motoristaId, escalaInvalida))
                .hasMessageContaining("fora da escala (1 a 5)");
    }
}
