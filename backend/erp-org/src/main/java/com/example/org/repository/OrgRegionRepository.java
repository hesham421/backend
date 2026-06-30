package com.example.org.repository;

import com.example.org.domain.OrgRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrgRegionRepository
        extends JpaRepository<OrgRegion, Long>, JpaSpecificationExecutor<OrgRegion> {

    boolean existsByNameArIgnoreCaseAndLegalEntityId(String nameAr, Long legalEntityId);
    boolean existsByNameEnIgnoreCaseAndLegalEntityId(String nameEn, Long legalEntityId);
    boolean existsByNameArIgnoreCaseAndLegalEntityIdAndIdNot(String nameAr, Long legalEntityId, Long id);
    boolean existsByNameEnIgnoreCaseAndLegalEntityIdAndIdNot(String nameEn, Long legalEntityId, Long id);
    boolean existsByRegionCode(String regionCode);

    @Query("SELECT COUNT(r) FROM OrgRegion r WHERE r.regionType.id = :regionTypeId AND r.isActiveFl = true")
    long countByRegionTypeId(@Param("regionTypeId") Long regionTypeId);

    @Query("SELECT r FROM OrgRegion r WHERE r.legalEntity.id = :leId AND r.isActiveFl = true ORDER BY r.nameEn")
    List<OrgRegion> findActiveByLegalEntityId(@Param("leId") Long leId);
}
