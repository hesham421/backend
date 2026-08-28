/**
 * Entity/DTO mappers for the Organization module — one @Component mapper per entity. Never sets
 * audit fields (owned by AuditEntityListener); updateEntityFromRequest() returns void and skips
 * immutable fields.
 */
package com.erp.org.mapper;
