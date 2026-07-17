package com.example.erp.file.repository;

import com.example.erp.file.entity.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileCategoryRepository
    extends JpaRepository<FileCategory, Long>,
            JpaSpecificationExecutor<FileCategory> {

    // GAP-FILE-003 — FileCategory dropdown options (SCR-FILE-001 upload form), active only,
    // scoped to the requesting module. English order is a stable default; the response carries
    // both nameAr/nameEn so the frontend resolves the display label per current locale.
    List<FileCategory> findByModuleCodeAndIsActiveFlTrueOrderByNameEnAsc(String moduleCode);
}
