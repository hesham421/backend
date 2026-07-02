/**
 * JPA persistence entities for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>Every entity extends {@code AuditableEntity} — uniform across all 8 entities
 *       (LegalEntity, Branch, Region, Department, CostCenter, ProfitCenter, LocationSite,
 *       RegionType)</li>
 *   <li>Persistence responsibility ONLY — business rules live in the sibling
 *       {@code org.domain} package, never on the entity itself (see domain-layer.md)</li>
 *   <li>Optimistic locking is not used — no VERSION column</li>
 *   <li>Deactivation via {@code isActiveFl = false}, exposed through {@code activate()} /
 *       {@code deactivate()} helpers — never a direct setter</li>
 * </ul>
 *
 * @see com.example.erp.common.domain.AuditableEntity
 * @see com.example.erp.org.domain
 */
package com.example.erp.org.entity;
