package com.example.org.repository;

import com.example.org.domain.OrgRegionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgRegionTypeRepository
        extends JpaRepository<OrgRegionType, Long>, JpaSpecificationExecutor<OrgRegionType> {

    boolean existsByRegionTypeCodeIgnoreCase(String code);
    boolean existsByRegionTypeCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByNameArIgnoreCase(String nameAr);
    boolean existsByNameEnIgnoreCase(String nameEn);
    boolean existsByNameArIgnoreCaseAndIdNot(String nameAr, Long id);
    boolean existsByNameEnIgnoreCaseAndIdNot(String nameEn, Long id);

    List<OrgRegionType> findAllByIsActiveFl(Boolean isActiveFl);
}
