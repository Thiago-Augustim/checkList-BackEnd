package br.com.checklist.controller;

import br.com.checklist.dto.VeiculoDtos.*;
import br.com.checklist.service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VeiculoDto>> listar(@RequestParam(required = false, defaultValue = "false") boolean apenasAtivos) {
        return ResponseEntity.ok(apenasAtivos ? veiculoService.listarAtivos() : veiculoService.listarTodos());
    }

    @PostMapping
    public ResponseEntity<VeiculoDto> criar(@Valid @RequestBody SaveVeiculoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(veiculoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoDto> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody SaveVeiculoRequest request
    ) {
        return ResponseEntity.ok(veiculoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VeiculoDto> alterarStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        return ResponseEntity.ok(veiculoService.alterarStatusAtivo(id, ativo));
    }
}
