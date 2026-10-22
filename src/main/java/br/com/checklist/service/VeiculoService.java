package br.com.checklist.service;

import br.com.checklist.domain.entity.Veiculo;
import br.com.checklist.dto.VeiculoDtos.*;
import br.com.checklist.exception.BusinessValidationException;
import br.com.checklist.exception.ResourceNotFoundException;
import br.com.checklist.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Transactional(readOnly = true)
    public List<VeiculoDto> listarTodos() {
        return veiculoRepository.findAllByOrderByPlacaAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VeiculoDto> listarAtivos() {
        return veiculoRepository.findByAtivoTrueOrderByPlacaAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public VeiculoDto criar(SaveVeiculoRequest request) {
        String placaNormalizada = request.placa().trim().toUpperCase().replace("-", "");
        if (veiculoRepository.existsByPlacaIgnoreCase(placaNormalizada)) {
            throw new BusinessValidationException("Já existe um veículo cadastrado com a placa: " + placaNormalizada);
        }

        Veiculo veiculo = new Veiculo(
                UUID.randomUUID(),
                placaNormalizada,
                request.modelo().trim(),
                request.tipo(),
                request.kmAtual(),
                true
        );

        return toDto(veiculoRepository.save(veiculo));
    }

    @Transactional
    public VeiculoDto atualizar(UUID id, SaveVeiculoRequest request) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + id));

        String placaNormalizada = request.placa().trim().toUpperCase().replace("-", "");
        veiculoRepository.findByPlacaIgnoreCase(placaNormalizada).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new BusinessValidationException("Já existe outro veículo com a placa: " + placaNormalizada);
            }
        });

        veiculo.setPlaca(placaNormalizada);
        veiculo.setModelo(request.modelo().trim());
        veiculo.setTipo(request.tipo());
        veiculo.setKmAtual(request.kmAtual());

        return toDto(veiculoRepository.save(veiculo));
    }

    @Transactional
    public VeiculoDto alterarStatusAtivo(UUID id, boolean ativo) {
        Veiculo veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado: " + id));
        veiculo.setAtivo(ativo);
        return toDto(veiculoRepository.save(veiculo));
    }

    public VeiculoDto toDto(Veiculo v) {
        return new VeiculoDto(
                v.getId(),
                v.getPlaca(),
                v.getModelo(),
                v.getTipo(),
                v.getKmAtual(),
                v.isAtivo(),
                v.getCriadoEm()
        );
    }
}
