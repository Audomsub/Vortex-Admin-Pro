package com.vortexadmin.repository;

import com.vortexadmin.entity.UserSession;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByUser(User user);
    List<UserSession> findByUserOrderByLoginAtDesc(User user);
    Optional<UserSession> findFirstByUserAndLogoutAtIsNullOrderByLoginAtDesc(User user);
    Optional<UserSession> findByIdAndUser(Long id, User user);
    List<UserSession> findByLoginAtAfter(java.time.LocalDateTime cutoff);

    @Query("SELECT s.country, COUNT(s) FROM UserSession s WHERE s.country IS NOT NULL GROUP BY s.country ORDER BY COUNT(s) DESC")
    List<Object[]> countByCountry();
}
