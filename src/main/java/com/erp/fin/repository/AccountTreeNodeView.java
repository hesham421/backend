package com.erp.fin.repository;

/** One account's place in the chart: its id and its parent's id ({@code null} for a root account). */
public interface AccountTreeNodeView {

    Long getAccountId();

    Long getParentId();
}
