package com.vortexadmin.repository;

import com.vortexadmin.entity.ApiKey;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link ApiKey} entities, providing standard CRUD operations
 * and custom methods for user-scoped key listing and lookup by hashed key value.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    /**
     * Returns all API keys belonging to the specified user, ordered by creation date descending
     * so the most recently created keys appear first.
     *
     * @param user the owner of the API keys
     * @return a list of the user's API keys, newest first
     */
    List<ApiKey> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Finds an API key by its stored hash.
     * Used during authentication to look up a key without storing the plaintext value.
     *
     * @param keyHash the SHA-256 (or equivalent) hash of the raw API key string
     * @return an {@link Optional} containing the matching {@link ApiKey}, or empty if not found
     */
    Optional<ApiKey> findByKeyHash(String keyHash);

    /**
     * Finds an API key by its primary key and owning user, ensuring that a user can only
     * access their own keys.
     *
     * @param id   the primary key of the API key record
     * @param user the expected owner of the API key
     * @return an {@link Optional} containing the matching {@link ApiKey}, or empty if not found
     *         or if the key belongs to a different user
     */
    Optional<ApiKey> findByIdAndUser(Long id, User user);
}
