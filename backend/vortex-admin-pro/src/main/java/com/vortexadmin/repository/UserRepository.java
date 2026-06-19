package com.vortexadmin.repository;

import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findByDeletedAtIsNull();
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // Dashboard aggregations (computed in the database instead of loading all rows)
    long countByDeletedAtIsNull();
    long countByStatusIgnoreCaseAndDeletedAtIsNull(String status);
    long countByDeletedAtIsNullAndCreatedAtLessThanEqual(LocalDateTime end);
    long countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtLessThanEqual(String status, LocalDateTime end);
    long countByDeletedAtIsNullAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByStatusIgnoreCaseAndDeletedAtIsNullAndCreatedAtBetween(String status, LocalDateTime start, LocalDateTime end);
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);
    List<User> findTop4ByDeletedAtIsNullOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(r.name, 'Unknown'), COUNT(u) FROM User u LEFT JOIN u.role r WHERE u.deletedAt IS NULL GROUP BY r.name")
    List<Object[]> countUsersByRole();
}
