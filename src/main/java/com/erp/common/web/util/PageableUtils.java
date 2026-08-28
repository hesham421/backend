package com.erp.common.web.util;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.BusinessException;
import com.erp.common.exception.CommonErrorCodes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Centralizes pagination constraint enforcement to avoid duplication across controllers.
 *
 * @see com.erp.common.web.config.CommonWebConfig for default pagination settings
 */
public final class PageableUtils {

    /**
     * Maximum allowed page number to prevent excessive pagination
     */
    public static final int MAX_PAGE_NUMBER = 10_000;
    
    /**
     * Minimum allowed page size
     */
    public static final int MIN_PAGE_SIZE = 1;
    
    /**
     * Maximum allowed page size to prevent memory issues
     */
    public static final int MAX_PAGE_SIZE = 100;

    private PageableUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * @throws BusinessException if pageable is null
     */
    public static Pageable enforceConstraints(Pageable pageable) {
        if (pageable == null) {
            throw new BusinessException(Status.VALIDATION_ERROR, CommonErrorCodes.PAGEABLE_NULL, "Pageable cannot be null");
        }

        // Enforce page number constraint
        int pageNumber = Math.min(pageable.getPageNumber(), MAX_PAGE_NUMBER);
        
        // Enforce page size constraints
        int pageSize = Math.min(
            Math.max(pageable.getPageSize(), MIN_PAGE_SIZE), 
            MAX_PAGE_SIZE
        );
        
        // Preserve sort order from original Pageable
        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }

    /**
     * For endpoints that need a different maximum page size than the global default.
     *
     * @throws BusinessException if pageable is null or customMaxSize is invalid
     */
    public static Pageable enforceConstraints(Pageable pageable, int customMaxSize) {
        if (pageable == null) {
            throw new BusinessException(Status.VALIDATION_ERROR, CommonErrorCodes.PAGEABLE_NULL, "Pageable cannot be null");
        }
        
        if (customMaxSize < MIN_PAGE_SIZE) {
            throw new BusinessException(
                Status.VALIDATION_ERROR,
                CommonErrorCodes.PAGEABLE_INVALID_MAX_SIZE,
                "Custom max size must be at least " + MIN_PAGE_SIZE
            );
        }

        int pageNumber = Math.min(pageable.getPageNumber(), MAX_PAGE_NUMBER);
        
        int pageSize = Math.min(
            Math.max(pageable.getPageSize(), MIN_PAGE_SIZE), 
            customMaxSize
        );
        
        return PageRequest.of(pageNumber, pageSize, pageable.getSort());
    }

    /**
     * For logging/monitoring clients that request excessive page sizes or numbers.
     */
    public static boolean violatesConstraints(Pageable pageable) {
        if (pageable == null) {
            return false;
        }
        
        return pageable.getPageNumber() > MAX_PAGE_NUMBER
            || pageable.getPageSize() < MIN_PAGE_SIZE
            || pageable.getPageSize() > MAX_PAGE_SIZE;
    }
}
