package br.com.checklist.service;

import br.com.checklist.domain.entity.Checklist;
import br.com.checklist.domain.entity.ChecklistResposta;
import br.com.checklist.domain.entity.RespostaFoto;
import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.dto.ChecklistDtos.*;
import br.com.checklist.exception.ResourceNotFoundException;
import br.com.checklist.repository.ChecklistRepository;
import br.com.checklist.repository.ChecklistTemplateRepository;
import br.com.checklist.repository.RespostaFotoRepository;
import br.com.checklist.repository.VeiculoRepository;
import br.com.checklist.storage.FotoStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChecklistConsultaService {

    private final ChecklistRepository checklistRepository;
    private final RespostaFotoRepository fotoRepository;
    private final VeiculoRepository veiculoRepository;
    private final ChecklistTemplateRepository templateRepository;
    private final FotoStorageService fotoStorageService;

    public ChecklistConsultaService(
            ChecklistRepository checklistRepository,
            RespostaFotoRepository fotoRepository,
            VeiculoRepository veiculoRepository,
            ChecklistTemplateRepository templateRepository,
            FotoStorageService fotoStorageService
    ) {
        this.checklistRepository = checklistRepository;
        this.fotoRepository = fotoRepository;
        this.veiculoRepository = veiculoRepository;
        this.templateRepository = templateRepository;
        this.fotoStorageService = fotoStorageService;
    }

    @Transactional(readOnly = true)
    public DashboardResumoDto obterResumoDashboard() {
        long total = checklistRepository.count();
        long comAvaria = checklistRepository.countByPossuiAvariaTrue();
        long aguardandoFotos = checklistRepository.countByStatusSincronizacao(StatusSincronizacao.AGUARDANDO_FOTOS);
        long veiculosAtivos = veiculoRepository.findByAtivoTrueOrderByPlacaAsc().size();
        long templatesAtivos = templateRepository.findAllActiveWithQuestions().size();

        return new DashboardResumoDto(
                total,
                comAvaria,
                aguardandoFotos,
                veiculosAtivos,
                templatesAtivos
        );
    }

    @Transactional(readOnly = true)
    public List<ChecklistResumoDto> listarChecklists(Boolean possuiAvaria, StatusSincronizacao status, UUID veiculoId) {
        return checklistRepository.findAllWithSummaryOrderByRealizadoEmDesc()
                .stream()
                .filter(c -> possuiAvaria == null || c.isPossuiAvaria() == possuiAvaria)
                .filter(c -> status == null || c.getStatusSincronizacao() == status)
                .filter(c -> veiculoId == null || c.getVeiculo().getId().equals(veiculoId))
                .map(this::toResumoDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChecklistDetalheDto buscarDetalhePorId(UUID id) {
        Checklist c = checklistRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist não encontrado: " + id));

        List<RespostaFoto> todasFotos = fotoRepository.findAllByChecklistId(id);
        Map<UUID, List<RespostaFoto>> fotosPorResposta = todasFotos.stream()
                .collect(Collectors.groupingBy(f -> f.getResposta().getId()));

        List<RespostaDetalheDto> respostasDto = c.getRespostas()
                .stream()
                .sorted(Comparator.comparingInt(r -> r.getPergunta().getOrdem()))
                .map(r -> toRespostaDetalheDto(r, fotosPorResposta.getOrDefault(r.getId(), List.of())))
                .toList();

        return new ChecklistDetalheDto(
                c.getId(),
                c.getTemplate().getId(),
                c.getTemplate().getTitulo(),
                c.getVeiculo().getId(),
                c.getVeiculo().getPlaca(),
                c.getVeiculo().getModelo(),
                c.getMotorista().getId(),
                c.getMotorista().getNome(),
                c.getKmVistoria(),
                c.getObservacoesGerais(),
                c.getLatitude(),
                c.getLongitude(),
                c.getStatusSincronizacao(),
                c.isPossuiAvaria(),
                c.isAlertaInconsistencia(),
                c.getMotivoInconsistencia(),
                c.getRealizadoEm(),
                c.getSincronizadoEm(),
                respostasDto
        );
    }

    @Transactional(readOnly = true)
    public RespostaFoto buscarEntidadeFoto(UUID fotoId) {
        return fotoRepository.findById(fotoId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada: " + fotoId));
    }

    public byte[] carregarBytesFoto(String storageKey) {
        return fotoStorageService.load(storageKey);
    }

    private ChecklistResumoDto toResumoDto(Checklist c) {
        return new ChecklistResumoDto(
                c.getId(),
                c.getTemplate().getId(),
                c.getTemplate().getTitulo(),
                c.getVeiculo().getId(),
                c.getVeiculo().getPlaca(),
                c.getVeiculo().getModelo(),
                c.getMotorista().getId(),
                c.getMotorista().getNome(),
                c.getKmVistoria(),
                c.getStatusSincronizacao(),
                c.isPossuiAvaria(),
                c.isAlertaInconsistencia(),
                c.getMotivoInconsistencia(),
                c.getRealizadoEm(),
                c.getSincronizadoEm()
        );
    }

    private RespostaDetalheDto toRespostaDetalheDto(ChecklistResposta r, List<RespostaFoto> fotos) {
        List<FotoDetalheDto> fotosDto = fotos.stream()
                .map(f -> new FotoDetalheDto(
                        f.getId(),
                        f.getStatusUpload(),
                        f.getStorageKey() != null ? "/api/v1/fotos/" + f.getId() + "/arquivo" : null,
                        f.getTamanhoBytes(),
                        f.getLatitude(),
                        f.getLongitude(),
                        f.getCapturadaEm(),
                        f.getEnviadaEm()
                ))
                .toList();

        return new RespostaDetalheDto(
                r.getId(),
                r.getPergunta().getId(),
                r.getPergunta().getEnunciado(),
                r.getPergunta().getTipoResposta(),
                r.getPergunta().getOrdem(),
                r.getValorResposta(),
                r.getObservacaoAvaria(),
                r.isAvariaDetectada(),
                fotosDto
        );
    }
}
