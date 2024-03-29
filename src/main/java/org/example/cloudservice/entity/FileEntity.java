package org.example.cloudservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "files", schema = "public")
@Where(clause = "is_deleted <> true")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hash;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long size;

    @Lob
    @Column(nullable = false)
    private byte[] fileBytes;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private UserEntity user;
}
