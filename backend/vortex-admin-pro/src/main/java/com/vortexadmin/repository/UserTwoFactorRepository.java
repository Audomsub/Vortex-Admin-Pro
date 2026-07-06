package com.vortexadmin.repository;

import com.vortexadmin.entity.UserTwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserTwoFactor} entities, providing standard CRUD
 * operations and a method to retrieve the two-factor authentication configuration for a user.
 */
@Repository
public interface UserTwoFactorRepository extends JpaRepository<UserTwoFactor, Long> {

    /**
     * Finds the two-factor authentication record for the specified user.
     * Each user has at most one 2FA record, so the result is wrapped in an {@link Optional}.
     *
     * @param userId the primary key of the owning user
     * @return an {@link Optional} containing the user's 2FA configuration if it exists,
     *         or empty if 2FA has not been set up for that user
     */
    Optional<UserTwoFactor> findByUserId(Long userId);
}
