/**
 * Application services for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>Interface + implementation per entity</li>
 *   <li>Orchestrates: validation delegation to {@code org.domain}, NumberingEngine calls,
 *       {@code org.mapper}, and {@code org.repository} — no business decisions inlined here</li>
 *   <li>Returns {@code ServiceResult<T>} (delete() is void)</li>
 *   <li>{@code @PreAuthorize} on every public method; {@code @Transactional} on writes,
 *       {@code @Transactional(readOnly = true)} on reads</li>
 *   <li>All exceptions are {@code LocalizedException} — {@code NotFoundException} is banned</li>
 * </ul>
 *
 * @see com.example.erp.org.domain
 * @see com.example.erp.common.domain.status.ServiceResult
 */
package com.example.erp.org.service;
