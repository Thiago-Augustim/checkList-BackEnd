package br.com.checklist.controller;

import br.com.checklist.dto.TemplateDtos.*;
import br.com.checklist.service.ChecklistTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/templates")
public class ChecklistTemplateController {

    private final ChecklistTemplateService templateService;

    public ChecklistTemplateController(ChecklistTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ResponseEntity<List<TemplateDto>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean incluirPerguntasInativas
    ) {
        return ResponseEntity.ok(templateService.listarTodos(incluirPerguntasInativas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> buscarPorId(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "false") boolean incluirPerguntasInativas
    ) {
        return ResponseEntity.ok(templateService.buscarPorId(id, incluirPerguntasInativas));
    }

    @PostMapping
    public ResponseEntity<TemplateDto> criar(@Valid @RequestBody SaveTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody SaveTemplateRequest request
    ) {
        return ResponseEntity.ok(templateService.atualizar(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TemplateDto> alterarStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean ativo = Boolean.TRUE.equals(body.get("ativo"));
        return ResponseEntity.ok(templateService.alterarStatusAtivo(id, ativo));
    }
}
