/**
 * Data Transfer Objects for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>Create/Update/Response/Search DTOs per entity</li>
 *   <li>Business Code and audit fields excluded from Create/Update input where Read-Only</li>
 *   <li>{@code SearchRequest} extends {@code BaseSearchContractRequest}; child search requests
 *       override {@code toCommonSearchRequest()} and expose a parent ID extractor</li>
 *   <li>{@code UsageResponse} exposes {@code canDelete}/{@code canDeactivate} + reason</li>
 *   <li>{@code OptionResponse} is slim — no audit fields</li>
 *   <li>Every validation message and UI-facing string carries AR + EN (bilingual)</li>
 * </ul>
 *
 * @see com.example.erp.common.dto.BaseSearchContractRequest
 */
package com.example.erp.org.dto;
