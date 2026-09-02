package com.erp.org.repository;

import com.erp.org.entity.OrgRegionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Minimal repository stood up ahead of RegionType's own Admin CRUD sub (DRV-ORG-015), solely so
 * {@link com.erp.org.service.RegionService} can resolve/validate the parent RegionType FK on
 * Region create.
 */
@Repository
public interface RegionTypeRepository
    extends JpaRepository<OrgRegionType, Long>,
            JpaSpecificationExecutor<OrgRegionType> {

    List<OrgRegionType> findByIsActiveFlTrueOrderByNameEnAsc();
}
