package com.example.erp.file.repository;

import com.example.erp.file.entity.FileDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDocumentRepository
    extends JpaRepository<FileDocument, Long>,
            JpaSpecificationExecutor<FileDocument> {
}
