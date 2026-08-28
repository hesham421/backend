package com.erp.common.web.config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.List;

/**
 * Standard pagination helpers each module's WebConfig should call for consistency
 * (default page size 20, max 100).
 */
public class CommonWebConfigurer {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public static PageableHandlerMethodArgumentResolver createPageableResolver() {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        
        // Set default pageable when none provided
        resolver.setFallbackPageable(PageRequest.of(0, DEFAULT_PAGE_SIZE));
        
        // Enforce maximum page size to prevent performance issues
        resolver.setMaxPageSize(MAX_PAGE_SIZE);
        
        return resolver;
    }

    public static Pageable enforcePaginationLimits(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(
                pageable.getPageNumber(), 
                MAX_PAGE_SIZE, 
                pageable.getSort()
            );
        }
        return pageable;
    }

    /**
     * Get default page size
     */
    public static int getDefaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * Get maximum allowed page size
     */
    public static int getMaxPageSize() {
        return MAX_PAGE_SIZE;
    }
}
