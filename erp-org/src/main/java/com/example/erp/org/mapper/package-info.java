/**
 * Entity &#8596; DTO mappers for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>One {@code @Component} mapper per entity — MapStruct or manual mapping</li>
 *   <li>Never sets audit fields — those are owned exclusively by {@code AuditEntityListener}</li>
 *   <li>Child {@code toEntity()} accepts the parent entity as an FK parameter</li>
 *   <li>{@code updateEntityFromRequest()} returns void and skips immutable fields</li>
 *   <li>{@code toResponse()} uses {@code Boolean.TRUE.equals()} for boolean checks</li>
 *   <li>All methods handle null input</li>
 * </ul>
 *
 * @see com.example.erp.common.audit.AuditEntityListener
 */
package com.example.erp.org.mapper;
