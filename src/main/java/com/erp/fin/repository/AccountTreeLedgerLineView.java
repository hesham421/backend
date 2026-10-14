package com.erp.fin.repository;

/** A ledger line of a parent account's ledger: the line plus the sub-account it was posted to. */
public interface AccountTreeLedgerLineView extends AccountLedgerLineView {

    Long getAccountId();

    String getAccountCode();

    String getAccountNameAr();

    String getAccountNameEn();
}
