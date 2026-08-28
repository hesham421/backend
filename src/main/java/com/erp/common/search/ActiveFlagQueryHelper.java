package com.erp.common.search;

import com.erp.common.converter.BooleanNumberConverter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds JPA query predicates for active/inactive flag filtering against NUMBER(1) columns;
 * a null filter value always matches all records rather than being treated as false.
 */
public final class ActiveFlagQueryHelper {

    /**
     * For native queries; pair with {@link BooleanNumberConverter#toDbValue(Boolean)}.
     */
    public static final String NATIVE_ACTIVE_CONDITION =
        "(:isActive IS NULL OR IS_ACTIVE = :isActive)";

    public static final String JPQL_ACTIVE_CONDITION =
        "(:isActive IS NULL OR e.isActive = :isActive)";

    private ActiveFlagQueryHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * A null {@code isActive} matches all records rather than being treated as false.
     */
    public static Predicate buildActivePredicate(
            CriteriaBuilder cb,
            Expression<Boolean> activeExpression,
            Boolean isActive) {
        
        if (isActive == null) {
            // Return a predicate that always evaluates to true (match all)
            return cb.conjunction();
        }
        
        // Return equality predicate
        return cb.equal(activeExpression, isActive);
    }

    /**
     * For entity fields mapped directly to Integer rather than through a converter.
     */
    public static Predicate buildActivePredicateForIntegerColumn(
            CriteriaBuilder cb,
            Expression<Integer> activeExpression,
            Boolean isActive) {
        
        if (isActive == null) {
            return cb.conjunction();
        }
        
        Integer dbValue = BooleanNumberConverter.toDbValue(isActive);
        return cb.equal(activeExpression, dbValue);
    }

    public static <T> Specification<T> hasActiveStatus(String fieldName, Boolean isActive) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get(fieldName), isActive);
        };
    }

    public static <T> Specification<T> isActive(String fieldName) {
        return hasActiveStatus(fieldName, Boolean.TRUE);
    }

    public static <T> Specification<T> isInactive(String fieldName) {
        return hasActiveStatus(fieldName, Boolean.FALSE);
    }

    public static Integer toNativeQueryParam(Boolean isActive) {
        return BooleanNumberConverter.toDbValue(isActive);
    }

    /**
     * {@code otherSpecs} may contain nulls, which are skipped.
     */
    @SafeVarargs
    public static <T> Specification<T> withActiveFilter(
            String activeFieldName,
            Boolean isActive,
            Specification<T>... otherSpecs) {
        
        Specification<T> result = hasActiveStatus(activeFieldName, isActive);
        
        if (otherSpecs != null) {
            for (Specification<T> spec : otherSpecs) {
                if (spec != null) {
                    result = result.and(spec);
                }
            }
        }
        
        return result;
    }
}
