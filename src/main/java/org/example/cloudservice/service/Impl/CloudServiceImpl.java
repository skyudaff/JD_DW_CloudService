package org.example.cloudservice.service.Impl;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
            log.error("File not attached: {}", fileName);
            throw new ErrorInputDataException(messageSource.getMessage("file.upload.error", null,
                    LocaleContextHolder.getLocale()), 400);
        }

        Long userId = jwtProvider.getAuthorizedUser().getId();

        Optional<FileEntity> existingFile = fileRepository.findFileEntityByFileName(fileName);
        if (existingFile.isPresent()) {
            FileEntity fileEntity = existingFile.get();
            if (fileEntity.isDelete()) {
                fileEntity.setDelete(false);
                fileRepository.save(fileEntity);
                log.info("File {} marked for deletion, updated to not deleted", fileName);
            } else {
                log.error("File with name {} already exists. Please upload another file", fileName);
                throw new ErrorInputDataException(messageSource.getMessage("file.uploaded.error", null,
                        LocaleContextHolder.getLocale()), userId);
            }
        } else {
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
                log.error("File processing error: {}", fileName, e);
                throw new ErrorInputDataException(messageSource.getMessage("file.process.error", null,
                        LocaleContextHolder.getLocale()), userId);
            }
        }
    }


    @Override
    public void deleteFile(String fileName) {
        Long userId = jwtProvider.getAuthorizedUser().getId();

        FileEntity file = getFileByFileName(fileName, userId);
        file.setDelete(true);
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
        return filesByUserIdWithLimit.stream().filter(file -> !file.isDelete())
                .map(file -> FileDto.builder()
                        .fileName(file.getFileName())
                        .hash(file.getHash())
                        .type(file.getType())
                        .date(file.getCreatedDate())
                        .size(file.getSize())
                        .fileBytes(file.getFileBytes())
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String calculateFileHash(MultipartFile file) {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try (InputStream fis = file.getInputStream();
             DigestInputStream dis = new DigestInputStream(fis, md)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = dis.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : Objects.requireNonNull(md.digest())) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private FileEntity getFileByFileName(String fileName, Long userId) {
        final var message = messageSource.getMessage("file.exist.error", null, LocaleContextHolder.getLocale());
        return fileRepository.findFileEntityByFileName(fileName)
                .orElseThrow(() -> new ErrorInputDataException(message, userId));
    }
}
