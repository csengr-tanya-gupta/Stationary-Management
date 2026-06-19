package com.stationery.auth.repository;

import com.stationery.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * 
     * WHY IT EXISTS:
     * Used heavily during the Login Process and by the CustomUserDetailsService to load user 
     * credentials for authentication.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, avoiding null checks
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given username.
     * 
     * WHY IT EXISTS:
     * Crucial for the User Management / Registration process to ensure usernames are unique.
     * It is faster than findByUsername because it translates to a SQL 'EXISTS' query rather 
     * than fetching the entire row.
     *
     * @param username the username to check
     * @return true if a user exists with the username
     */
    Boolean existsByUsername(String username);

    /**
     * Check if a user exists with the given email.
     * 
     * WHY IT EXISTS:
     * Used during registration to enforce the business rule that an email can only be associated 
     * with one account.
     *
     * @param email the email to check
     * @return true if a user exists with the email
     */
    Boolean existsByEmail(String email);
}
