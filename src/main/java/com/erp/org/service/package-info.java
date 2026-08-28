/**
 * Application services for the Organization module — one interface + implementation per entity,
 * orchestrating validation delegation, NumberingEngine, mapper, and repository calls. Returns
 * ServiceResult<T> (delete() is void); @PreAuthorize on every public method.
 */
package com.erp.org.service;
