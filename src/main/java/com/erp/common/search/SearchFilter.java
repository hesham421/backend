package com.erp.common.search;

import java.util.Objects;

/**
 * A single search filter criterion: field (dot notation for nested properties), operator,
 * and value (may be null for IS_NULL/IS_NOT_NULL).
 */
public class SearchFilter {

    private String field;
    private Op op;
    private Object value;

    /**
     * Default constructor for deserialization.
     */
    public SearchFilter() {
    }

    public SearchFilter(String field, Op op, Object value) {
        this.field = field;
        this.op = op;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchFilter that = (SearchFilter) o;
        return Objects.equals(field, that.field) &&
               op == that.op &&
               Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, op, value);
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
               "field='" + field + '\'' +
               ", op=" + op +
               ", value=" + value +
               '}';
    }
}
