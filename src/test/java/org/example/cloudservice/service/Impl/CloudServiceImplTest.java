package org.example.cloudservice.service.Impl;

import org.example.cloudservice.security.JwtTokenProvider;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.entity.FileEntity;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.exception.ErrorInputDataException;
import org.example.cloudservice.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudServiceImplTest {
    @Mock
    MessageSource messageSource;
    @Mock
    FileRepository fileRepository;
    @Mock
    JwtTokenProvider jwtProvider;
    @InjectMocks
    CloudServiceImpl cloudService;

    FileEntity fileEntity;
    UserEntity userEntity;
    FileDto fileDTO;
    static final String FILE_NAME = "test.txt";

    @BeforeEach
    void createTestUserEntity() {
        userEntity = UserEntity.builder().id(1L).build();
    }

    @BeforeEach
    void createTestFileEntity() {
        fileEntity = FileEntity.builder()
                .id(1L)
                .hash("982d9e3eb996f559e633f4d194def3761d909f5a3b647d1a851fead67c32c9d1")
                .fileName(FILE_NAME)
                .type("text/plain")
                .size(1L)
                .fileBytes("text".getBytes())
                .createdDate(LocalDateTime.now())
                .user(userEntity)
                .build();
    }


    @Test
    void uploadFile_ValidFile_Success() {
        // Arrange
        MultipartFile multipartFile = createMockMultipartFile();

        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFileEntityByFileName(FILE_NAME)).thenReturn(Optional.empty());
        when(fileRepository.save(any(FileEntity.class))).thenReturn(fileEntity);

        // Act
        assertDoesNotThrow(() -> cloudService.uploadFile(FILE_NAME, multipartFile));

        // Assert
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void uploadFile_EmptyFile_ThrowsErrorInputDataException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", FILE_NAME, "text/plain", new byte[0]);

        when(messageSource.getMessage(eq("file.upload.error"), any(), eq(Locale.getDefault())))
                .thenReturn("File not attached");

        // Act & Assert
        assertThrows(ErrorInputDataException.class, () -> cloudService.uploadFile(FILE_NAME, emptyFile));
    }

    @Test
    void uploadFile_AlreadyExists_ThrowsErrorInputDataException() {
        // Arrange
        MultipartFile nonEmptyFile =
                new MockMultipartFile("file", FILE_NAME, "text/plain", "file content".getBytes());

        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFileEntityByFileName(FILE_NAME)).thenReturn(Optional.of(new FileEntity()));
        when(messageSource.getMessage(eq("file.uploaded.error"), any(), eq(Locale.getDefault())))
                .thenReturn("This file already uploaded. Please upload other file");

        // Act & Assert
        assertThrows(ErrorInputDataException.class, () -> cloudService.uploadFile(FILE_NAME, nonEmptyFile));
    }

    private MultipartFile createMockMultipartFile() {
        return new MockMultipartFile("file", FILE_NAME, "text/plain", "file content".getBytes());
    }

    @Test
    void deleteFile_DeletedSuccessfully() {
        // Arrange
        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFileEntityByFileName(FILE_NAME)).thenReturn(Optional.of(fileEntity));

        // Act
        assertDoesNotThrow(() -> cloudService.deleteFile(FILE_NAME));

        // Assert
        assertTrue(fileEntity.isDeleted());
        assertNotNull(fileEntity.getCreatedDate());
        verify(fileRepository, times(1)).save(any(FileEntity.class));
    }

    @Test
    void downloadFile_ReturnsFileDTO() {
        // Arrange
        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFileEntityByFileName(FILE_NAME)).thenReturn(Optional.of(fileEntity));

        // Act
        fileDTO = cloudService.downloadFile(FILE_NAME);

        // Assert
        assertNotNull(fileDTO);
        assertEquals(FILE_NAME, fileDTO.fileName());
        assertEquals(fileEntity.getType(), fileDTO.type());
        assertArrayEquals(fileEntity.getFileBytes(), fileDTO.fileBytes());
    }

    @Test
    void editFileName_EditedSuccessfully() {
        // Arrange
        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFileEntityByFileName(anyString())).thenReturn(Optional.empty());
        FileDto fileDTO = FileDto.builder().fileName("new_name.txt").build();
        when(fileRepository.findFileEntityByFileName(FILE_NAME)).thenReturn(Optional.of(fileEntity));

        // Act
        assertDoesNotThrow(() -> cloudService.editFileName(FILE_NAME, fileDTO));

        // Assert
        assertEquals(fileDTO.fileName(), fileEntity.getFileName());
        verify(fileRepository, times(1)).save(fileEntity);
    }

    @Test
    void getFileList_ValidLimit_ReturnsFileDTOList() {
        // Arrange
        List<FileEntity> fileEntities = List.of(FileEntity.builder().fileName("file1.txt").build(),
                FileEntity.builder().fileName("file2.txt").build());
        when(jwtProvider.getAuthorizedUser()).thenReturn(userEntity);
        when(fileRepository.findFilesByUserIdWithLimit(userEntity.getId(), 2)).thenReturn(fileEntities);

        // Act
        List<FileDto> result = cloudService.getFileList(2);

        // Assert
        assertNotNull(result);
        assertEquals(fileEntities.size(), result.size());
        assertEquals(fileEntities.get(0).getFileName(), result.get(0).fileName());
        assertEquals(fileEntities.get(1).getFileName(), result.get(1).fileName());
    }
}
