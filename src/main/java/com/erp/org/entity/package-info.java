/**
 * JPA persistence entities for the Organization module — all extend AuditableEntity. Persistence
 * responsibility only; business rules live in the sibling org.domain package. Deactivation is
 * via isActiveFl, exposed through activate()/deactivate() helpers, never a direct setter.
 */
package com.erp.org.entity;
