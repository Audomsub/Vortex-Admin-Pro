package com.vortexadmin.repository;

import com.vortexadmin.entity.UserTwoFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTwoFactorRepository extends JpaRepository<UserTwoFactor, Long> {
    Optional<UserTwoFactor> findByUserId(Long userId);
}
