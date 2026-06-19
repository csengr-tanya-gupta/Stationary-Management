package com.stationery.auth.model;

/**
 * Enumeration representing user roles in the system.
 * 
 * WHY IT EXISTS & ROLE MANAGEMENT:
 * Defines the strict set of authorities a user can have. By using an Enum rather than 
 * a plain String, we ensure type safety and prevent typos (e.g., "STDENT" instead of "STUDENT") 
 * across the application.
 * 
 * REAL-WORLD EXPLANATION:
 * Think of roles as ID badge colors. An ADMIN (red badge) can access the management back-office 
 * (like adding inventory), while a STUDENT (blue badge) can only access the storefront to buy items.
 * 
 * VIVA/INTERVIEW PREPARATION:
 * - Q: How is this stored in the database? 
 *   A: In the User entity, we use @Enumerated(EnumType.STRING). If we used EnumType.ORDINAL, it would store 0 for ADMIN and 1 for STUDENT, which causes problems if we later change the order of the enums.
 */
public enum Role {
    ADMIN,
    STUDENT
}
