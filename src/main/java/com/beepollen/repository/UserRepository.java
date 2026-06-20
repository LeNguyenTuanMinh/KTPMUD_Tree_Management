package com.beepollen.repository;

import com.beepollen.entity.User;
import com.beepollen.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the {@link User} entity.
 * Provides CRUD operations and custom query methods for user management,
 * including lookups by username, email, and role.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    Boolean existsByUsername(String username);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    Boolean existsByEmail(String email);

    /**
     * Finds all users assigned a specific role.
     *
     * @param role the role to filter by
     * @return a list of users with the specified role
     */
    List<User> findByRole(Role role);

    /**
     * Counts the number of users with a specific role and enabled status.
     *
     * @param role the role to count
     * @param enabled the enabled status to count
     * @return the number of matching users
     */
    long countByRoleAndEnabled(Role role, boolean enabled);
}
