package br.com.checklist.storage;

import java.util.UUID;

public interface FotoStorageService {
    String store(UUID fotoId, String originalFilename, String contentType, byte[] content);
    byte[] load(String storageKey);
}
