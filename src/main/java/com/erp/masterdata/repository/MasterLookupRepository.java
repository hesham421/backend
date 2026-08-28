package com.erp.masterdata.repository;

import com.erp.masterdata.entity.MdMasterLookup;
import com.erp.masterdata.repository.projection.LookupValueProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterLookupRepository extends JpaRepository<MdMasterLookup, Long>, 
                                                  JpaSpecificationExecutor<MdMasterLookup> {

    Optional<MdMasterLookup> findByLookupKey(String lookupKey);

    /**
     * Single native query with JOIN — master lookup status + all active detail values in one
     * round-trip (avoids N+1).
     */
    @Query(value = """
            SELECT 
                ml.ID_PK as masterLookupId,
                ml.LOOKUP_KEY as lookupKey,
                ml.IS_ACTIVE as masterIsActive,
                ld.CODE as code,
                ld.NAME_AR as nameAr,
                ld.NAME_EN as nameEn,
                ld.SORT_ORDER as sortOrder,
                ld.IS_ACTIVE as detailIsActive
            FROM MD_MASTER_LOOKUP ml
            LEFT JOIN MD_LOOKUP_DETAIL ld 
                ON ml.ID_PK = ld.MASTER_LOOKUP_ID_FK 
                AND ld.IS_ACTIVE = :isActive
            WHERE ml.LOOKUP_KEY = :lookupKey
            ORDER BY ld.SORT_ORDER ASC, ld.NAME_AR ASC
            """, nativeQuery = true)
    List<LookupValueProjection> findLookupValuesByKey(
            @Param("lookupKey") String lookupKey,
            @Param("isActive") Integer isActive);

    boolean existsByLookupKey(String lookupKey);

    /**
     * Single query with JOIN validates the code exists and is active under an active master lookup
     * key, in one round-trip.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM MD_MASTER_LOOKUP ml
            INNER JOIN MD_LOOKUP_DETAIL ld
                ON ml.ID_PK = ld.MASTER_LOOKUP_ID_FK
            WHERE ml.LOOKUP_KEY = :lookupKey
              AND ml.IS_ACTIVE = 1
              AND ld.CODE = :code
              AND ld.IS_ACTIVE = 1
            """, nativeQuery = true)
    int countActiveByKeyAndCode(
            @Param("lookupKey") String lookupKey,
            @Param("code") String code);

    Page<MdMasterLookup> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT COUNT(ld) FROM MdLookupDetail ld WHERE ld.masterLookup.id = :masterLookupId")
    long countLookupDetails(@Param("masterLookupId") Long masterLookupId);

    @Query("SELECT COUNT(ld) FROM MdLookupDetail ld " +
           "WHERE ld.masterLookup.id = :masterLookupId AND ld.isActive = true")
    long countActiveLookupDetails(@Param("masterLookupId") Long masterLookupId);
}
