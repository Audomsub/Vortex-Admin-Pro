package com.vortexadmin.repository;

import com.vortexadmin.entity.RefreshToken;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RefreshToken} entities, providing standard CRUD operations
 * and methods for token-value lookup and user-scoped token deletion during logout.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a refresh token by its raw token string.
     *
     * @param token the token string to look up
     * @return an {@link Optional} containing the matching {@link RefreshToken}, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with the specified user.
     * Called on logout or when all sessions for a user are revoked.
     *
     * @param user the owner of the refresh tokens to delete
     */
    void deleteByUser(User user);
}
