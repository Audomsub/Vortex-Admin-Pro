package com.vortexadmin.repository;

import com.vortexadmin.entity.ApiKey;
import com.vortexadmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByUserOrderByCreatedAtDesc(User user);
    Optional<ApiKey> findByKeyHash(String keyHash);
    Optional<ApiKey> findByIdAndUser(Long id, User user);
}
