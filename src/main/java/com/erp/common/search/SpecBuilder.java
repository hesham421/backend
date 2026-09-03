package com.erp.common.search;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class SpecBuilder {

    private SpecBuilder() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    public static <T> Specification<T> build(SearchRequest searchRequest, SetAllowedFields allowedFields,
                                              FieldValueConverter valueConverter) {
        return (root, query, criteriaBuilder) -> {
            if (searchRequest == null || searchRequest.getFilters() == null || searchRequest.getFilters().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            for (SearchFilter filter : searchRequest.getFilters()) {
                if (!allowedFields.isAllowed(filter.getField())) {
                    continue;
                }
                Object value = valueConverter.convert(filter.getField(), filter.getValue());
                if (value == null) {
                    continue;
                }
                predicates.add(toPredicate(root, criteriaBuilder, filter.getField(), filter.getOperator(), value));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate toPredicate(Root<?> root, CriteriaBuilder cb, String field,
                                          SearchOperator operator, Object value) {
        return switch (operator) {
            case EQUALS -> cb.equal(root.get(field), value);
            case NOT_EQUALS -> cb.notEqual(root.get(field), value);
            case LIKE -> cb.like(cb.lower(root.get(field).as(String.class)),
                "%" + String.valueOf(value).toLowerCase() + "%");
            case GREATER_THAN -> cb.greaterThan(asComparablePath(root, field), (Comparable) value);
            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo(asComparablePath(root, field), (Comparable) value);
            case LESS_THAN -> cb.lessThan(asComparablePath(root, field), (Comparable) value);
            case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo(asComparablePath(root, field), (Comparable) value);
            case IN -> root.get(field).in((List<?>) value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Path<Comparable> asComparablePath(Root<?> root, String field) {
        return (Path<Comparable>) (Path) root.get(field);
    }
}
