package com.vortexadmin.repository;

import com.vortexadmin.entity.PasswordHistory;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link PasswordHistory} entities, providing standard CRUD
 * operations and a method to retrieve a user's recent password history for reuse prevention.
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    /**
     * Returns the five most recent password history entries for the specified user, ordered by
     * change timestamp descending.  Used by the password-policy service to prevent users from
     * re-using their recent passwords.
     *
     * @param user the user whose recent password history should be retrieved
     * @return a list of up to five most-recent {@link PasswordHistory} records for the user
     */
    List<PasswordHistory> findTop5ByUserOrderByChangedAtDesc(User user);
}
