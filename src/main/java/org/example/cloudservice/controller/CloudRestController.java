package org.example.cloudservice.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudservice.dto.FileDto;
import org.example.cloudservice.service.Impl.CloudServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cloud")
@PreAuthorize("hasAuthority('ROLE_USER')")
@RequiredArgsConstructor
public class CloudRestController {
    private final CloudServiceImpl fileService;

    @PostMapping("file")
    public ResponseEntity<Void> handleUploadFile(@RequestParam("filename") String fileName,
                                                 @NotNull @RequestPart("file") MultipartFile file) {
        fileService.uploadFile(fileName, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("file")
    public ResponseEntity<Void> handleDeleteFile(@RequestParam("filename") String fileName) {
        fileService.deleteFile(fileName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("file")
    @ResponseBody
    public ResponseEntity<byte[]> handleDownloadFile(@RequestParam String filename) {
        FileDto file = fileService.downloadFile(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.type()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.fileName() + "\"")
                .body(file.fileBytes());
    }

    @PutMapping("file")
    public ResponseEntity<Void> handleEditFileName(@RequestParam String filename, @RequestBody FileDto fileDTO) {
        fileService.editFileName(filename, fileDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("list")
    public ResponseEntity<List<FileDto>> handleGetFileList(@Min(0) @RequestParam int limit) {
        return ResponseEntity.ok(fileService.getFileList(limit));
    }
}
