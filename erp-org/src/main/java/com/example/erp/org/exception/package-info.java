/**
 * Module-specific exceptions for the Organization module.
 *
 * <h2>Architecture Rules</h2>
 * <ul>
 *   <li>{@code LocalizedException} subclasses bound to {@code ERR-ORG-*} error codes
 *       (see master-registry.md, Section A)</li>
 *   <li>{@code NotFoundException} is banned — use {@code LocalizedException} with
 *       {@code Status.NOT_FOUND} instead</li>
 *   <li>Every {@code ERR-ORG-*} message carries AR + EN</li>
 * </ul>
 *
 * @see com.example.erp.common.exception.LocalizedException
 */
package com.example.erp.org.exception;
