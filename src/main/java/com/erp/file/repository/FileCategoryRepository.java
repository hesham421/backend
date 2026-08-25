package com.erp.file.repository;

import com.erp.file.entity.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileCategoryRepository
    extends JpaRepository<FileCategory, Long>,
            JpaSpecificationExecutor<FileCategory> {
}
