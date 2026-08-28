package com.erp.masterdata.repository;

import com.erp.masterdata.entity.MdLookupDetail;
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
public interface LookupDetailRepository extends JpaRepository<MdLookupDetail, Long>, 
                                                  JpaSpecificationExecutor<MdLookupDetail> {

    Optional<MdLookupDetail> findByMasterLookupIdAndCode(Long masterLookupId, String code);

    boolean existsByMasterLookupIdAndCode(Long masterLookupId, String code);

    boolean existsByMasterLookupIdAndCodeAndIdNot(Long masterLookupId, String code, Long id);

    Page<MdLookupDetail> findByMasterLookupId(Long masterLookupId, Pageable pageable);

    Page<MdLookupDetail> findByMasterLookupIdAndIsActive(
        Long masterLookupId, Boolean isActive, Pageable pageable);

    /**
     * Explicit FETCH JOIN on {@code masterLookup} to avoid N+1 when searching.
     */
    @Query(value = "SELECT ld FROM MdLookupDetail ld " +
           "WHERE ld.masterLookup.id = :masterLookupId",
           countQuery = "SELECT COUNT(ld) FROM MdLookupDetail ld " +
           "WHERE ld.masterLookup.id = :masterLookupId")
    Page<MdLookupDetail> searchByMasterLookupId(
        @Param("masterLookupId") Long masterLookupId,
        Pageable pageable);

    /**
     * Explicit FETCH JOIN on {@code masterLookup} (with active filter) to avoid N+1.
     */
    @Query(value = "SELECT ld FROM MdLookupDetail ld " +
           "WHERE ld.masterLookup.id = :masterLookupId " +
           "AND ld.isActive = :isActive",
           countQuery = "SELECT COUNT(ld) FROM MdLookupDetail ld " +
           "WHERE ld.masterLookup.id = :masterLookupId " +
           "AND ld.isActive = :isActive")
    Page<MdLookupDetail> searchByMasterLookupIdAndActive(
        @Param("masterLookupId") Long masterLookupId,
        @Param("isActive") Boolean isActive,
        Pageable pageable);

    /**
     * Ordered by sortOrder for dropdown display.
     */
    @Query("SELECT ld FROM MdLookupDetail ld " +
           "JOIN ld.masterLookup ml " +
           "WHERE ml.lookupKey = :lookupKey " +
           "AND ld.isActive = :isActive " +
           "ORDER BY ld.sortOrder ASC, ld.nameAr ASC")
    List<MdLookupDetail> findByMasterLookupKeyAndActive(
        @Param("lookupKey") String lookupKey,
        @Param("isActive") Boolean isActive);

}
