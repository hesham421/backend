/**
 * REST controllers for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>One controller per SCR-ID-aligned resource: LegalEntityController, BranchController,
 *       RegionController, DepartmentController, CostCenterController, ProfitCenterController,
 *       LocationSiteController</li>
 *   <li>ZERO business logic — orchestration only, delegates to {@code org.service}</li>
 *   <li>Injects ONLY the entity's service(s) and {@code OperationCode}</li>
 *   <li>Search endpoints use {@code POST /search} with {@code @RequestBody} — never GET with
 *       {@code @ModelAttribute}</li>
 *   <li>Activation uses separate {@code PUT /{id}/activate} and {@code PUT /{id}/deactivate}
 *       endpoints — never a single toggle-active endpoint</li>
 * </ul>
 *
 * @see com.example.erp.common.web.OperationCode
 */
package com.example.erp.org.controller;
