package com.erp.masterdata.repository.projection;

public interface LookupValueProjection {
    
    /**
     * Master lookup ID
     */
    Long getMasterLookupId();
    
    /**
     * Master lookup key
     */
    String getLookupKey();
    
    /**
     * Master lookup active status
     */
    Integer getMasterIsActive();
    
    /**
     * Lookup detail code/value
     */
    String getCode();
    
    /**
     * Lookup detail Arabic name
     */
    String getNameAr();
    
    /**
     * Lookup detail English name
     */
    String getNameEn();
    
    /**
     * Lookup detail sort order
     */
    Integer getSortOrder();
    
    /**
     * Lookup detail active status
     */
    Integer getDetailIsActive();
}
