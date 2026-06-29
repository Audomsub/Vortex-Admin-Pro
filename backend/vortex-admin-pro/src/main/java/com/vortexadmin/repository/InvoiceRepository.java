package com.vortexadmin.repository;

import com.vortexadmin.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT i FROM Invoice i WHERE i.subscription.organization.id = :organizationId ORDER BY i.issuedAt DESC")
    List<Invoice> findByOrganizationId(@Param("organizationId") Long organizationId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issuedAt >= :from")
    BigDecimal sumPaidAmountSince(@Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.status = 'PAID' AND i.issuedAt BETWEEN :from AND :to")
    BigDecimal sumPaidAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Invoice> findByStatusAndIssuedAtBetweenOrderByIssuedAtAsc(String status, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query(value = "DELETE FROM invoices WHERE subscription_id IN (SELECT id FROM subscriptions WHERE organization_id = :organizationId)", nativeQuery = true)
    void deleteByOrganizationId(@Param("organizationId") Long organizationId);
}
