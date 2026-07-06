package com.vortexadmin.repository;

import com.vortexadmin.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserPreference} entities, providing standard CRUD
 * operations and a method to look up a user's preference record by their user ID.
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * Finds the preference record associated with the specified user.
     * Each user has at most one preference record, so the result is wrapped in an
     * {@link Optional}.
     *
     * @param userId the primary key of the owning user
     * @return an {@link Optional} containing the user's preferences if they exist,
     *         or empty if no preference record has been created yet
     */
    Optional<UserPreference> findByUserId(Long userId);
}
