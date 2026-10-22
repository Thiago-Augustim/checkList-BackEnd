package br.com.checklist.controller;

import br.com.checklist.dto.SyncDtos.*;
import br.com.checklist.service.ChecklistSyncService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sync")
public class ChecklistSyncController {

    private final ChecklistSyncService syncService;

    public ChecklistSyncController(ChecklistSyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/catalogos")
    public ResponseEntity<CatalogoSyncResponse> obterCatalogos() {
        return ResponseEntity.ok(syncService.obterCatalogoAtivo());
    }

    @PostMapping("/checklists")
    public ResponseEntity<SyncChecklistResponse> sincronizarChecklist(
            Authentication authentication,
            @Valid @RequestBody SyncChecklistRequest request
    ) {
        UUID motoristaId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(syncService.sincronizarChecklist(motoristaId, request));
    }

    @PostMapping(value = "/fotos/{fotoId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoUploadResponse> uploadFoto(
            @PathVariable UUID fotoId,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        return ResponseEntity.ok(syncService.receberUploadFoto(fotoId, arquivo));
    }
}
