package org.example.cloudservice.repository;

import org.example.cloudservice.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Optional<FileEntity> findFileEntityByFileName(String fileName);

    @Query(value = "select * from files f where f.user_id = :userId order by f.file_name limit :limit", nativeQuery = true)
    List<FileEntity> findFilesByUserIdWithLimit(Long userId, int limit);
}
