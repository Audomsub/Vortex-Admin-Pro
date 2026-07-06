package com.vortexadmin.repository;

import com.vortexadmin.entity.PasswordResetToken;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PasswordResetToken} entities, providing standard CRUD
 * operations and methods for token-value lookup and user-scoped token cleanup.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a password-reset token by its raw token string.
     *
     * @param token the token string to look up
     * @return an {@link Optional} containing the matching {@link PasswordResetToken},
     *         or empty if not found or already deleted
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Deletes all password-reset tokens associated with the specified user.
     * Called after a successful password reset to invalidate any remaining tokens.
     *
     * @param user the owner of the tokens to delete
     */
    void deleteByUser(User user);
}
