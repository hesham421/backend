package com.example.erp.org.repository;

import com.example.erp.org.entity.OrgRegionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Minimal repository stood up ahead of RegionType's own Admin-only CRUD sub (DRV-ORG-015) solely
 * to let {@link com.example.erp.org.service.RegionService} resolve/validate the parent
 * RegionType FK on Region create. Extended with full CRUD support when RegionType's own sub
 * is executed.
 */
@Repository
public interface RegionTypeRepository
    extends JpaRepository<OrgRegionType, Long>,
            JpaSpecificationExecutor<OrgRegionType> {
}
