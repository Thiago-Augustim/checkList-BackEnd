package br.com.checklist.service;

import br.com.checklist.domain.entity.ChecklistTemplate;
import br.com.checklist.domain.entity.TemplatePergunta;
import br.com.checklist.domain.enums.TipoVeiculo;
import br.com.checklist.dto.TemplateDtos.*;
import br.com.checklist.exception.ResourceNotFoundException;
import br.com.checklist.repository.ChecklistRespostaRepository;
import br.com.checklist.repository.ChecklistTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class ChecklistTemplateService {

    private final ChecklistTemplateRepository templateRepository;
    private final ChecklistRespostaRepository respostaRepository;

    public ChecklistTemplateService(
            ChecklistTemplateRepository templateRepository,
            ChecklistRespostaRepository respostaRepository
    ) {
        this.templateRepository = templateRepository;
        this.respostaRepository = respostaRepository;
    }

    @Transactional(readOnly = true)
    public List<TemplateDto> listarTodos(boolean incluirPerguntasInativas) {
        return templateRepository.findAllWithQuestionsOrderByCriadoEmDesc()
                .stream()
                .map(t -> toDto(t, incluirPerguntasInativas))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TemplateDto> listarAtivosParaCatalogo() {
        return templateRepository.findAllActiveWithQuestions()
                .stream()
                .map(t -> toDto(t, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public TemplateDto buscarPorId(UUID id, boolean incluirPerguntasInativas) {
        ChecklistTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de checklist não encontrado: " + id));
        return toDto(template, incluirPerguntasInativas);
    }

    @Transactional
    public TemplateDto criar(SaveTemplateRequest request) {
        ChecklistTemplate template = new ChecklistTemplate();
        template.setId(UUID.randomUUID());
        template.setTitulo(request.titulo().trim());
        template.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        template.setTipoVeiculo(request.tipoVeiculo() != null ? request.tipoVeiculo() : TipoVeiculo.CAVALO);
        template.setAtivo(true);

        List<TemplatePergunta> perguntas = new ArrayList<>();
        int index = 1;
        for (SavePerguntaRequest pReq : request.perguntas()) {
            TemplatePergunta p = criarNovaPergunta(template, pReq, index++);
            perguntas.add(p);
        }
        template.setPerguntas(perguntas);

        return toDto(templateRepository.save(template), false);
    }

    /**
     * Atualiza o modelo preservando a imutabilidade e Soft Delete (ativo = false)
     * para perguntas que já possuem respostas vinculadas em vistorias anteriores.
     */
    @Transactional
    public TemplateDto atualizar(UUID id, SaveTemplateRequest request) {
        ChecklistTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de checklist não encontrado: " + id));

        template.setTitulo(request.titulo().trim());
        template.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        if (request.tipoVeiculo() != null) {
            template.setTipoVeiculo(request.tipoVeiculo());
        }
        template.setAtualizadoEm(OffsetDateTime.now());

        Map<UUID, TemplatePergunta> perguntasExistentes = new HashMap<>();
        for (TemplatePergunta p : template.getPerguntas()) {
            perguntasExistentes.put(p.getId(), p);
        }

        Set<UUID> idsMantidosAtivos = new HashSet<>();
        int index = 1;

        for (SavePerguntaRequest pReq : request.perguntas()) {
            int ordem = pReq.ordem() != null ? pReq.ordem() : index;
            index++;

            if (pReq.id() != null && perguntasExistentes.containsKey(pReq.id())) {
                TemplatePergunta existente = perguntasExistentes.get(pReq.id());
                boolean mudouEstrutura = !existente.getEnunciado().equals(pReq.enunciado().trim())
                        || existente.getTipoResposta() != pReq.tipoResposta();

                boolean jaPossuiRespostas = respostaRepository.existsByPerguntaId(existente.getId());

                if (mudouEstrutura && jaPossuiRespostas) {
                    // Regra de Imutabilidade + Soft Delete: inativa a pergunta antiga e cria uma nova versão com novo UUID
                    existente.setAtivo(false);
                    TemplatePergunta novaVersao = criarNovaPergunta(template, pReq, ordem);
                    template.getPerguntas().add(novaVersao);
                    idsMantidosAtivos.add(novaVersao.getId());
                } else {
                    // Pode atualizar metadados ou pergunta ainda não respondida
                    existente.setEnunciado(pReq.enunciado().trim());
                    existente.setTipoResposta(pReq.tipoResposta());
                    existente.setOrdem(ordem);
                    existente.setObrigatoria(pReq.obrigatoria());
                    existente.setExigeFotoSeAvaria(pReq.exigeFotoSeAvaria());
                    existente.setValorGatilhoAvaria(normalizarGatilho(pReq));
                    existente.setAtivo(true);
                    idsMantidosAtivos.add(existente.getId());
                }
            } else {
                TemplatePergunta nova = criarNovaPergunta(template, pReq, ordem);
                template.getPerguntas().add(nova);
                idsMantidosAtivos.add(nova.getId());
            }
        }

        // Qualquer pergunta anterior que não veio na lista sofre Soft Delete (ativo = false), nunca DELETE físico
        for (TemplatePergunta existente : template.getPerguntas()) {
            if (!idsMantidosAtivos.contains(existente.getId())) {
                existente.setAtivo(false);
            }
        }

        return toDto(templateRepository.save(template), false);
    }

    @Transactional
    public TemplateDto alterarStatusAtivo(UUID id, boolean ativo) {
        ChecklistTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de checklist não encontrado: " + id));
        template.setAtivo(ativo);
        template.setAtualizadoEm(OffsetDateTime.now());
        return toDto(templateRepository.save(template), false);
    }

    private TemplatePergunta criarNovaPergunta(ChecklistTemplate template, SavePerguntaRequest req, int ordemDefault) {
        TemplatePergunta p = new TemplatePergunta();
        p.setId(UUID.randomUUID());
        p.setTemplate(template);
        p.setEnunciado(req.enunciado().trim());
        p.setTipoResposta(req.tipoResposta());
        p.setOrdem(req.ordem() != null ? req.ordem() : ordemDefault);
        p.setObrigatoria(req.obrigatoria());
        p.setExigeFotoSeAvaria(req.exigeFotoSeAvaria());
        p.setValorGatilhoAvaria(normalizarGatilho(req));
        p.setAtivo(true);
        return p;
    }

    private String normalizarGatilho(SavePerguntaRequest req) {
        if (req.valorGatilhoAvaria() != null && !req.valorGatilhoAvaria().isBlank()) {
            return req.valorGatilhoAvaria().trim().toUpperCase();
        }
        if (!req.exigeFotoSeAvaria()) {
            return null;
        }
        return switch (req.tipoResposta()) {
            case BOOLEANO -> "FALSE";
            case ESCALA_1_5 -> "2";
            case TEXTO -> null;
        };
    }

    public TemplateDto toDto(ChecklistTemplate t, boolean incluirPerguntasInativas) {
        List<PerguntaDto> perguntas = t.getPerguntas()
                .stream()
                .filter(p -> incluirPerguntasInativas || p.isAtivo())
                .sorted(Comparator.comparingInt(TemplatePergunta::getOrdem))
                .map(p -> new PerguntaDto(
                        p.getId(),
                        p.getEnunciado(),
                        p.getTipoResposta(),
                        p.getOrdem(),
                        p.isObrigatoria(),
                        p.isExigeFotoSeAvaria(),
                        p.getValorGatilhoAvaria(),
                        p.isAtivo()
                ))
                .toList();

        return new TemplateDto(
                t.getId(),
                t.getTitulo(),
                t.getDescricao(),
                t.getTipoVeiculo(),
                t.isAtivo(),
                t.getCriadoEm(),
                t.getAtualizadoEm(),
                perguntas
        );
    }
}
