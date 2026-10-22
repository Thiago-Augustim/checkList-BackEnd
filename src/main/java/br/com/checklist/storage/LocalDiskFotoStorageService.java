package br.com.checklist.storage;

import br.com.checklist.exception.BusinessValidationException;
import br.com.checklist.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class LocalDiskFotoStorageService implements FotoStorageService {

    private final Path rootDir;

    public LocalDiskFotoStorageService(@Value("${app.storage.upload-dir:./uploads/fotos}") String uploadDir) {
        this.rootDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível inicializar o diretório de upload de fotos", e);
        }
    }

    @Override
    public String store(UUID fotoId, String originalFilename, String contentType, byte[] content) {
        if (content == null || content.length == 0) {
            throw new BusinessValidationException("O arquivo de foto enviado está vazio.");
        }
        String ext = ".jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            String candidate = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (candidate.matches("\\.(jpg|jpeg|png|webp)")) {
                ext = candidate;
            }
        }
        String fileName = fotoId.toString() + ext;
        Path targetPath = this.rootDir.resolve(fileName).normalize();
        if (!targetPath.startsWith(this.rootDir)) {
            throw new BusinessValidationException("Caminho de arquivo inválido.");
        }
        try {
            Files.write(targetPath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new BusinessValidationException("Falha ao gravar foto de avaria no storage.");
        }
    }

    @Override
    public byte[] load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new ResourceNotFoundException("Foto ainda não possui arquivo armazenado.");
        }
        Path filePath = this.rootDir.resolve(storageKey).normalize();
        if (!filePath.startsWith(this.rootDir) || !Files.exists(filePath)) {
            throw new ResourceNotFoundException("Arquivo de foto não encontrado no storage: " + storageKey);
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Erro ao ler arquivo de foto do storage.");
        }
    }
}
