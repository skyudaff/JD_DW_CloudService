package org.example.cloudservice.service.Impl;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.exception.ErrorInputDataException;
import org.example.cloudservice.repository.FileRepository;
import org.example.cloudservice.security.JwtTokenProvider;
import org.example.cloudservice.service.CloudService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CloudServiceImpl implements CloudService {
    private final MessageSource messageSource;
    private final FileRepository fileRepository;
    private final JwtTokenProvider jwtProvider;

    @Override
    public void uploadFile(String fileName, @NonNull MultipartFile file) {
        if (file.isEmpty()) {
            handleEmptyFile(fileName);
            return;
        }

        Long userId = jwtProvider.getAuthorizedUser().getId();

        Optional<FileEntity> existingFile = fileRepository.findFileEntityByFileName(fileName);
        if (existingFile.isPresent()) {
            handleExistingFile(fileName, userId, existingFile.get());
        } else {
            handleNewFile(fileName, userId, file);
        }
    }

    private void handleEmptyFile(String fileName) {
        log.error("File not attached: {}", fileName);
        throw new ErrorInputDataException(
                messageSource.getMessage("file.upload.error", null, LocaleContextHolder.getLocale()), 400);
    }

    private void handleExistingFile(String fileName, Long userId, FileEntity existingFile) {
        if (existingFile.isDeleted()) {
            handleDeletedFile(fileName, existingFile);
        } else {
            handleDuplicateFile(fileName, userId);
        }
    }

    private void handleDeletedFile(String fileName, FileEntity deletedFile) {
        deletedFile.setDeleted(false);
        fileRepository.save(deletedFile);
        log.info("File {} marked for deletion, updated to not deleted", fileName);
    }

    private void handleDuplicateFile(String fileName, Long userId) {
        log.error("File with name {} already exists. Please upload another file", fileName);
        throw new ErrorInputDataException(
                messageSource.getMessage("file.uploaded.error", null, LocaleContextHolder.getLocale()), userId);
    }

    private void handleNewFile(String fileName, Long userId, MultipartFile file) {
        try {
            String hash = calculateFileHash(file);
            byte[] fileBytes = file.getBytes();

            fileRepository.save(FileEntity.builder()
                    .hash(hash)
                    .fileName(fileName)
                    .type(file.getContentType())
                    .size(file.getSize())
                    .fileBytes(fileBytes)
                    .createdDate(LocalDateTime.now())
                    .user(UserEntity.builder().id(userId).build())
                    .build());

            log.info("File {} created and saved to storage", fileName);
        } catch (IOException e) {
            handleFileProcessingError(fileName, userId);
        }
    }

    private void handleFileProcessingError(String fileName, Long userId) {
        log.error("File processing error: {}", fileName);
        throw new ErrorInputDataException(
                messageSource.getMessage("file.process.error", null, LocaleContextHolder.getLocale()), userId);
    }


    @Override
    public void deleteFile(String fileName) {
        Long userId = jwtProvider.getAuthorizedUser().getId();

        FileEntity file = getFileByFileName(fileName, userId);
        file.setDeleted(true);
        file.setCreatedDate(LocalDateTime.now());

        log.info("Set flag isDelete on file from storage " +
                "by file name {} and userID {}", file.getFileName(), userId);
        fileRepository.save(file);
    }

    @Override
    public FileDto downloadFile(String fileName) {
        Long userId = jwtProvider.getAuthorizedUser().getId();
        FileEntity file = getFileByFileName(fileName, userId);

        log.info("Download file: {}", fileName);
        return FileDto.builder()
                .fileName(file.getFileName())
                .type(file.getType())
                .fileBytes(file.getFileBytes())
                .build();
    }

    @Override
    public void editFileName(String fileName, FileDto fileDTO) {
        Long userId = jwtProvider.getAuthorizedUser().getId();

        Optional<FileEntity> existingFileWithNewName = fileRepository.findFileEntityByFileName(fileDTO.fileName());
        if (existingFileWithNewName.isPresent()) {
            log.error("File with name {} already exists. Please upload another file", fileName);
            throw new ErrorInputDataException(messageSource.getMessage("file.uploaded.error", null,
                    LocaleContextHolder.getLocale()), userId);
        }

        FileEntity file = getFileByFileName(fileName, userId);
        file.setFileName(fileDTO.fileName());

        log.info("Edit name file: {} to {}", fileName, fileDTO.fileName());
        fileRepository.save(file);
    }

    @Override
    public List<FileDto> getFileList(int limit) {
        Long userId = jwtProvider.getAuthorizedUser().getId();

        List<FileEntity> filesByUserIdWithLimit = fileRepository.findFilesByUserIdWithLimit(userId, limit);
        return filesByUserIdWithLimit.stream()
                .filter(file -> !file.isDeleted())
                .map(this::mapFileEntityToDto)
                .collect(Collectors.toList());
    }

    private FileDto mapFileEntityToDto(FileEntity file) {
        return FileDto.builder()
                .fileName(file.getFileName())
                .hash(file.getHash())
                .type(file.getType())
                .date(file.getCreatedDate())
                .size(file.getSize())
                .fileBytes(file.getFileBytes())
                .build();
    }

    private String calculateFileHash(MultipartFile file) throws IOException {
        MessageDigest md = DigestUtils.getSha256Digest();
        try (InputStream fis = file.getInputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }
        return DigestUtils.sha256Hex(md.digest());
    }

    private FileEntity getFileByFileName(String fileName, Long userId) {
        final var message = messageSource.getMessage("file.exist.error", null, LocaleContextHolder.getLocale());
        return fileRepository.findFileEntityByFileName(fileName)
                .orElseThrow(() -> new ErrorInputDataException(message, userId));
    }
}
