/**
 * REST controllers for the Organization module — one per SCR-ID-aligned resource, orchestration
 * only (zero business logic, delegates to org.service). Search uses POST /search +
 * @RequestBody; activation uses separate activate/deactivate endpoints, never a single toggle.
 */
package com.erp.org.controller;
