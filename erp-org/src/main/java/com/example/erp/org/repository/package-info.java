/**
 * Spring Data repositories for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>Extends {@code JpaRepository<Entity, Long>} AND {@code JpaSpecificationExecutor}</li>
 *   <li>Supports {@code SpecBuilder} + {@code PageableBuilder} driven search and tree
 *       retrieval</li>
 *   <li>Never injected outside the {@code erp-org} module</li>
 *   <li>Existence checks use {@code existsBy<Field>()}; update-uniqueness checks use
 *       {@code existsBy<Field>AndIdNot()} only when the field is mutable</li>
 *   <li>Child queries use {@code JOIN FETCH}</li>
 * </ul>
 *
 * @see com.example.erp.common.search.SpecBuilder
 * @see com.example.erp.common.search.PageableBuilder
 */
package com.example.erp.org.repository;
