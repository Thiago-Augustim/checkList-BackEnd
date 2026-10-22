package br.com.checklist.controller;

import br.com.checklist.domain.entity.RespostaFoto;
import br.com.checklist.domain.enums.StatusSincronizacao;
import br.com.checklist.dto.ChecklistDtos.*;
import br.com.checklist.service.ChecklistConsultaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ChecklistConsultaController {

    private final ChecklistConsultaService consultaService;

    public ChecklistConsultaController(ChecklistConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResumoDto> obterDashboard() {
        return ResponseEntity.ok(consultaService.obterResumoDashboard());
    }

    @GetMapping("/checklists")
    public ResponseEntity<List<ChecklistResumoDto>> listarChecklists(
            @RequestParam(required = false) Boolean possuiAvaria,
            @RequestParam(required = false) StatusSincronizacao status,
            @RequestParam(required = false) UUID veiculoId
    ) {
        return ResponseEntity.ok(consultaService.listarChecklists(possuiAvaria, status, veiculoId));
    }

    @GetMapping("/checklists/{id}")
    public ResponseEntity<ChecklistDetalheDto> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(consultaService.buscarDetalhePorId(id));
    }

    @GetMapping("/fotos/{fotoId}/arquivo")
    public ResponseEntity<byte[]> baixarArquivoFoto(@PathVariable UUID fotoId) {
        RespostaFoto foto = consultaService.buscarEntidadeFoto(fotoId);
        byte[] bytes = consultaService.carregarBytesFoto(foto.getStorageKey());
        String contentType = foto.getMimeType() != null ? foto.getMimeType() : MediaType.IMAGE_JPEG_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(bytes);
    }
}
