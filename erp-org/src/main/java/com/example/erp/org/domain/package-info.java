/**
 * Business Rule Domain objects for the Organization module.
 *
 * <p>Per {@code domain-layer.md}: the Domain object is <b>not</b> the JPA entity. It is a
 * separate, plain class that answers "is this operation allowed?" for a single entity
 * (deactivation guards, code-immutability, tree-cycle prevention, state-transition checks).
 * The Entity remains persistence-only; the Service remains orchestration-only.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>NO Spring or JPA annotations ({@code @Component}, {@code @Service}, {@code @Entity},
 *       {@code @Transactional})</li>
 *   <li>NO Repository or database access, under any circumstance — all data is passed in as
 *       plain arguments by the Service</li>
 *   <li>Constructed ONLY via static factory methods: {@code create(...)}, {@code from(...)}</li>
 *   <li>Throws {@code LocalizedException} for all business rule violations</li>
 *   <li>Does not call another module's service — cross-module data is resolved by the Service
 *       and passed in as a plain argument</li>
 *   <li>At most one Domain object per entity; a Domain Service exists only when a rule
 *       genuinely spans multiple entities</li>
 * </ul>
 *
 * @see com.example.erp.org.entity
 * @see com.example.erp.org.service
 */
package com.example.erp.org.domain;
