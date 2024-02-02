package org.example.cloudservice.service;

import org.example.cloudservice.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudService {
    void uploadFile(String fileName, MultipartFile file);

    void deleteFile(String fileName);

    FileDto downloadFile(String fileName);

    void editFileName(String fileName, FileDto fileDTO);

    List<FileDto> getFileList(int limit);
}
