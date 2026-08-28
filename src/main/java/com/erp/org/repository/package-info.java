/**
 * Spring Data repositories for the Organization module — extend JpaRepository +
 * JpaSpecificationExecutor, never injected outside erp-org. Existence checks use
 * existsBy<Field>(); update-uniqueness checks add AndIdNot().
 */
package com.erp.org.repository;
