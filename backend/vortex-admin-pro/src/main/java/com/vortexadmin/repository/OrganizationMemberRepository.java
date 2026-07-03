package com.vortexadmin.repository;

import com.vortexadmin.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
    List<OrganizationMember> findByOrganizationId(Long organizationId);

    @Query("SELECT m.user.id FROM OrganizationMember m WHERE m.organization.id = :organizationId")
    List<Long> findUserIdsByOrganizationId(@Param("organizationId") Long organizationId);
    List<OrganizationMember> findByUserId(Long userId);
    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
    long countByOrganizationId(Long organizationId);

    @Modifying
    @Query(value = "DELETE FROM organization_members WHERE organization_id = :organizationId", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
