package br.com.checklist.service;

import br.com.checklist.domain.entity.*;
import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.domain.enums.StatusUploadFoto;
import br.com.checklist.dto.SyncDtos.*;
import br.com.checklist.exception.BusinessValidationException;
import br.com.checklist.exception.ResourceNotFoundException;
import br.com.checklist.repository.*;
import br.com.checklist.storage.FotoStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ChecklistSyncService {

    private final VeiculoService veiculoService;
    private final ChecklistTemplateService templateService;
    private final ChecklistRepository checklistRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final TemplatePerguntaRepository perguntaRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RespostaFotoRepository fotoRepository;
    private final FotoStorageService fotoStorageService;

    public ChecklistSyncService(
            VeiculoService veiculoService,
            ChecklistTemplateService templateService,
            ChecklistRepository checklistRepository,
            ChecklistTemplateRepository templateRepository,
            TemplatePerguntaRepository perguntaRepository,
            VeiculoRepository veiculoRepository,
            UsuarioRepository usuarioRepository,
            RespostaFotoRepository fotoRepository,
            FotoStorageService fotoStorageService
    ) {
        this.veiculoService = veiculoService;
        this.templateService = templateService;
        this.checklistRepository = checklistRepository;
        this.templateRepository = templateRepository;
        this.perguntaRepository = perguntaRepository;
        this.veiculoRepository = veiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.fotoRepository = fotoRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public CatalogoSyncResponse obterCatalogoAtivo() {
        return new CatalogoSyncResponse(
                OffsetDateTime.now(),
                veiculoService.listarAtivos(),
                templateService.listarAtivosParaCatalogo()
        );
    }

    /**
     * Etapa 1 da Sincronização Offline: Recebe o JSON leve do checklist.
     * Implementa Idempotência pelo UUID gerado no Flutter e Aceitação Tolerante para itens inativados.
     */
    @Transactional
    public SyncChecklistResponse sincronizarChecklist(UUID motoristaId, SyncChecklistRequest request) {
        // 1. Verificação de Idempotência: se o UUID já foi sincronizado antes, retorna o estado atual sem duplicar
        Optional<Checklist> existenteOpt = checklistRepository.findById(request.id());
        if (existenteOpt.isPresent()) {
            Checklist existente = existenteOpt.get();
            List<UUID> pendentes = fotoRepository
                    .findByChecklistIdAndStatusUpload(existente.getId(), StatusUploadFoto.PENDENTE)
                    .stream()
                    .map(RespostaFoto::getId)
                    .toList();

            return new SyncChecklistResponse(
                    existente.getId(),
                    existente.getStatusSincronizacao(),
                    existente.isAlertaInconsistencia(),
                    existente.getMotivoInconsistencia(),
                    pendentes
            );
        }

        Usuario motorista = usuarioRepository.findById(motoristaId)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: " + motoristaId));

        Veiculo veiculo = veiculoRepository.findById(request.veiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + request.veiculoId()));

        ChecklistTemplate template = templateRepository.findById(request.templateId())
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de checklist não encontrado: " + request.templateId()));

        List<String> inconsistencias = new ArrayList<>();
        if (!motorista.isAtivo()) {
            inconsistencias.add("Motorista estava inativo no momento da sincronização");
        }
        if (!veiculo.isAtivo()) {
            inconsistencias.add("Veículo (" + veiculo.getPlaca() + ") foi inativado antes da sincronização");
        }
        if (!template.isAtivo()) {
            inconsistencias.add("Modelo de checklist foi inativado antes da sincronização");
        }
        if (template.getTipoVeiculo() != null && template.getTipoVeiculo() != veiculo.getTipo()) {
            inconsistencias.add(
                    "Modelo de checklist (" + template.getTipoVeiculo() + ") difere do tipo do veículo (" + veiculo.getTipo() + ")"
            );
        }

        Checklist checklist = new Checklist();
        checklist.setId(request.id());
        checklist.setTemplate(template);
        checklist.setVeiculo(veiculo);
        checklist.setMotorista(motorista);
        checklist.setKmVistoria(request.kmVistoria());
        checklist.setObservacoesGerais(request.observacoesGerais());
        checklist.setLatitude(request.latitude());
        checklist.setLongitude(request.longitude());
        checklist.setRealizadoEm(request.realizadoEm());
        checklist.setSincronizadoEm(OffsetDateTime.now());

        // Atualiza quilometragem do veículo caso a vistoria registre um hodômetro maior
        if (request.kmVistoria() > veiculo.getKmAtual()) {
            veiculo.setKmAtual(request.kmVistoria());
            veiculoRepository.save(veiculo);
        }

        boolean possuiAlgumaAvaria = false;
        List<UUID> fotosPendentesIds = new ArrayList<>();
        List<ChecklistResposta> entidadesRespostas = new ArrayList<>();

        for (SyncRespostaRequest respReq : request.respostas()) {
            TemplatePergunta pergunta = perguntaRepository.findById(respReq.perguntaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pergunta não encontrada: " + respReq.perguntaId()));

            if (!pergunta.isAtivo()) {
                inconsistencias.add("Pergunta '" + pergunta.getEnunciado() + "' foi inativada ou editada enquanto o app estava offline");
            }

            String valorNormalizado = validarEConverterValorResposta(pergunta, respReq.valorResposta());
            boolean avariaNesteItem = respReq.avariaDetectada() || detectarAvariaPorGatilho(pergunta, valorNormalizado);

            List<SyncFotoEsperadaRequest> fotosEsperadas = respReq.fotosEsperadas() != null
                    ? respReq.fotosEsperadas()
                    : List.of();

            if (avariaNesteItem && pergunta.isExigeFotoSeAvaria() && fotosEsperadas.isEmpty()) {
                throw new BusinessValidationException(
                        "A pergunta '" + pergunta.getEnunciado() + "' exige pelo menos uma foto da avaria quando não conforme."
                );
            }

            if (avariaNesteItem) {
                possuiAlgumaAvaria = true;
            }

            ChecklistResposta resposta = new ChecklistResposta();
            resposta.setId(respReq.id());
            resposta.setChecklist(checklist);
            resposta.setPergunta(pergunta);
            resposta.setValorResposta(valorNormalizado);
            resposta.setObservacaoAvaria(respReq.observacaoAvaria());
            resposta.setAvariaDetectada(avariaNesteItem);

            List<RespostaFoto> fotos = new ArrayList<>();
            for (SyncFotoEsperadaRequest fReq : fotosEsperadas) {
                RespostaFoto foto = new RespostaFoto();
                foto.setId(fReq.id());
                foto.setResposta(resposta);
                foto.setStatusUpload(StatusUploadFoto.PENDENTE);
                foto.setCapturadaEm(fReq.capturadaEm());
                foto.setLatitude(fReq.latitude());
                foto.setLongitude(fReq.longitude());
                fotos.add(foto);
                fotosPendentesIds.add(foto.getId());
            }

            resposta.setFotos(fotos);
            entidadesRespostas.add(resposta);
        }

        checklist.setRespostas(entidadesRespostas);
        checklist.setPossuiAvaria(possuiAlgumaAvaria);

        if (!inconsistencias.isEmpty()) {
            checklist.setAlertaInconsistencia(true);
            checklist.setMotivoInconsistencia(String.join("; ", inconsistencias));
        }

        StatusSincronizacao statusInicial = fotosPendentesIds.isEmpty()
                ? StatusSincronizacao.CONCLUIDO
                : StatusSincronizacao.AGUARDANDO_FOTOS;
        checklist.setStatusSincronizacao(statusInicial);

        checklistRepository.save(checklist);

        return new SyncChecklistResponse(
                checklist.getId(),
                checklist.getStatusSincronizacao(),
                checklist.isAlertaInconsistencia(),
                checklist.getMotivoInconsistencia(),
                fotosPendentesIds
        );
    }

    /**
     * Etapa 2 da Sincronização Offline: Recebe o arquivo binário de uma foto de avaria.
     * Quando todas as fotos pendentes de um checklist terminam de subir, atualiza o checklist para CONCLUIDO.
     */
    @Transactional
    public FotoUploadResponse receberUploadFoto(UUID fotoId, MultipartFile arquivo) {
        RespostaFoto foto = fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de foto esperada não encontrado: " + fotoId));

        Checklist checklist = foto.getResposta().getChecklist();

        // Idempotência no upload de mídia: se já foi concluído anteriormente, retorna 200 sem reprocessar
        if (foto.getStatusUpload() == StatusUploadFoto.CONCLUIDO) {
            return new FotoUploadResponse(
                    foto.getId(),
                    checklist.getId(),
                    foto.getStatusUpload(),
                    checklist.getStatusSincronizacao(),
                    "/api/v1/fotos/" + foto.getId() + "/arquivo"
            );
        }

        if (arquivo == null || arquivo.isEmpty()) {
            throw new BusinessValidationException("Arquivo de imagem não enviado ou vazio.");
        }

        byte[] bytes;
        try {
            bytes = arquivo.getBytes();
        } catch (IOException e) {
            throw new BusinessValidationException("Erro ao ler bytes do arquivo enviado.");
        }

        String storageKey = fotoStorageService.store(
                foto.getId(),
                arquivo.getOriginalFilename(),
                arquivo.getContentType(),
                bytes
        );

        foto.setStorageKey(storageKey);
        foto.setMimeType(arquivo.getContentType() != null ? arquivo.getContentType() : "image/jpeg");
        foto.setTamanhoBytes((long) bytes.length);
        foto.setStatusUpload(StatusUploadFoto.CONCLUIDO);
        foto.setEnviadaEm(OffsetDateTime.now());
        fotoRepository.save(foto);

        // Verifica se ainda restam fotos PENDENTES neste checklist
        List<RespostaFoto> restantes = fotoRepository.findByChecklistIdAndStatusUpload(
                checklist.getId(),
                StatusUploadFoto.PENDENTE
        );

        if (restantes.isEmpty() && checklist.getStatusSincronizacao() != StatusSincronizacao.CONCLUIDO) {
            checklist.setStatusSincronizacao(StatusSincronizacao.CONCLUIDO);
            checklistRepository.save(checklist);
        }

        return new FotoUploadResponse(
                foto.getId(),
                checklist.getId(),
                foto.getStatusUpload(),
                checklist.getStatusSincronizacao(),
                "/api/v1/fotos/" + foto.getId() + "/arquivo"
        );
    }

    /**
     * Valida e converte o valor em texto (VARCHAR) de acordo com o TipoResposta configurado na pergunta.
     */
    public String validarEConverterValorResposta(TemplatePergunta pergunta, String valorBruto) {
        String valor = valorBruto != null ? valorBruto.trim() : "";
        if (pergunta.isObrigatoria() && valor.isEmpty()) {
            throw new BusinessValidationException("A pergunta obrigatória '" + pergunta.getEnunciado() + "' não foi respondida.");
        }

        return switch (pergunta.getTipoResposta()) {
            case BOOLEANO -> {
                if ("TRUE".equalsIgnoreCase(valor) || "SIM".equalsIgnoreCase(valor) || "1".equals(valor)) {
                    yield "TRUE";
                } else if ("FALSE".equalsIgnoreCase(valor) || "NAO".equalsIgnoreCase(valor) || "NÃO".equalsIgnoreCase(valor) || "0".equals(valor)) {
                    yield "FALSE";
                }
                throw new BusinessValidationException(
                        "Valor inválido '" + valor + "' para pergunta booleana '" + pergunta.getEnunciado() + "'. Esperado: TRUE ou FALSE."
                );
            }
            case ESCALA_1_5 -> {
                try {
                    int nota = Integer.parseInt(valor);
                    if (nota < 1 || nota > 5) {
                        throw new BusinessValidationException(
                                "Valor fora da escala (1 a 5) para a pergunta '" + pergunta.getEnunciado() + "': " + nota
                        );
                    }
                    yield String.valueOf(nota);
                } catch (NumberFormatException e) {
                    throw new BusinessValidationException(
                            "Valor não numérico '" + valor + "' para pergunta de escala (1 a 5): '" + pergunta.getEnunciado() + "'."
                    );
                }
            }
            case TEXTO -> valor;
        };
    }

    private boolean detectarAvariaPorGatilho(TemplatePergunta pergunta, String valorNormalizado) {
        String gatilho = pergunta.getValorGatilhoAvaria();
        if (gatilho == null || gatilho.isBlank()) {
            return false;
        }
        return switch (pergunta.getTipoResposta()) {
            case BOOLEANO -> gatilho.trim().equalsIgnoreCase(valorNormalizado);
            case ESCALA_1_5 -> {
                try {
                    int nota = Integer.parseInt(valorNormalizado);
                    int notaGatilho = Integer.parseInt(gatilho.replace("<=", "").trim());
                    yield nota <= notaGatilho;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case TEXTO -> false;
        };
    }
}
