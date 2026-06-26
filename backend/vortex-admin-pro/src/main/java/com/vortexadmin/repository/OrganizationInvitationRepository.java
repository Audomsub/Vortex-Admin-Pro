package com.vortexadmin.repository;

import com.vortexadmin.entity.OrganizationInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, Long> {
    Optional<OrganizationInvitation> findByToken(String token);
    List<OrganizationInvitation> findByOrganizationId(Long organizationId);
    List<OrganizationInvitation> findByEmailAndStatus(String email, String status);
    boolean existsByOrganizationIdAndEmailAndStatus(Long organizationId, String email, String status);

    @Modifying
    @Query(value = "DELETE FROM organization_invitations WHERE organization_id = :organizationId", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
