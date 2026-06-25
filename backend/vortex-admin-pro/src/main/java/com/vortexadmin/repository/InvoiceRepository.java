package com.vortexadmin.repository;

import com.vortexadmin.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
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

    List<Invoice> findByStatusAndIssuedAtBetweenOrderByIssuedAtAsc(String status, LocalDateTime start, LocalDateTime end);
}
