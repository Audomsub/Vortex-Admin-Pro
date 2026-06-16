package com.vortexadmin.repository;

import com.vortexadmin.entity.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, Long> {
    Optional<OrganizationInvitation> findByToken(String token);
    List<OrganizationInvitation> findByOrganizationId(Long organizationId);
    List<OrganizationInvitation> findByEmailAndStatus(String email, String status);
    boolean existsByOrganizationIdAndEmailAndStatus(Long organizationId, String email, String status);
    void deleteByOrganizationId(Long organizationId);
}
