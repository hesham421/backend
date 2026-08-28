package com.erp.masterdata.exception;

/**
 * Centralized error codes for the MasterData module; every code needs matching entries in the English and Arabic i18n message files.
 */
public final class MasterDataErrorCodes {

    private MasterDataErrorCodes() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String MASTER_LOOKUP_NOT_FOUND = "MASTER_LOOKUP_NOT_FOUND";
    public static final String MASTER_LOOKUP_ACCESS_DENIED = "MASTER_LOOKUP_ACCESS_DENIED";
    public static final String MASTER_LOOKUP_IN_USE = "MASTER_LOOKUP_IN_USE";
    public static final String MASTER_LOOKUP_KEY_DUPLICATE = "MASTER_LOOKUP_KEY_DUPLICATE";
    public static final String MASTER_LOOKUP_ACTIVE_DETAILS_EXIST = "MASTER_LOOKUP_ACTIVE_DETAILS_EXIST";
    public static final String MASTER_LOOKUP_DETAILS_EXIST = "MASTER_LOOKUP_DETAILS_EXIST";
    public static final String MASTER_LOOKUP_FK_VIOLATION = "MASTER_LOOKUP_FK_VIOLATION";
    public static final String MASTER_LOOKUP_INACTIVE = "MASTER_LOOKUP_INACTIVE";
    public static final String LOOKUP_VALUE_INVALID = "LOOKUP_VALUE_INVALID";

    public static final String LOOKUP_DETAIL_NOT_FOUND = "LOOKUP_DETAIL_NOT_FOUND";
    public static final String LOOKUP_DETAIL_ACCESS_DENIED = "LOOKUP_DETAIL_ACCESS_DENIED";
    public static final String LOOKUP_DETAIL_CODE_DUPLICATE = "LOOKUP_DETAIL_CODE_DUPLICATE";
    public static final String LOOKUP_DETAIL_IN_USE = "LOOKUP_DETAIL_IN_USE";
    public static final String LOOKUP_DETAIL_FK_VIOLATION = "LOOKUP_DETAIL_FK_VIOLATION";
}
