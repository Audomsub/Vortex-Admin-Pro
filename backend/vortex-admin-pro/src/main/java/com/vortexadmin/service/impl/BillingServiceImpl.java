package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.UpgradePlanRequest;
import com.vortexadmin.dto.response.DiscountEligibilityResponse;
import com.vortexadmin.dto.response.InvoiceResponse;
import com.vortexadmin.dto.response.PlanResponse;
import com.vortexadmin.dto.response.SubscriptionResponse;
import com.vortexadmin.entity.*;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.*;
import com.vortexadmin.service.BillingService;
import com.vortexadmin.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final List<String> MANAGER_ROLES = List.of("OWNER", "ADMIN");

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final FileRepository fileRepository;

    private Organization requireManagedOrganization(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));
        Long userId = SecurityUtils.getCurrentUserId();
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this organization"));
        if (!MANAGER_ROLES.contains(member.getRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You must be an owner or admin to manage billing");
        }
        return org;
    }

    private PlanResponse mapToResponse(SubscriptionPlan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxUsers(plan.getMaxUsers())
                .maxStorageMb(plan.getMaxStorageMb())
                .build();
    }

    private SubscriptionResponse mapToResponse(Subscription subscription) {
        Organization org = subscription.getOrganization();
        long currentUsers = memberRepository.countByOrganizationId(org.getId());

        List<Long> memberUserIds = memberRepository.findUserIdsByOrganizationId(org.getId());
        Long rawStorage = memberUserIds.isEmpty() ? null : fileRepository.sumFileSizeByUserIds(memberUserIds);
        long storageUsedBytes = rawStorage != null ? rawStorage : 0L;

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .organizationId(org.getId())
                .organizationName(org.getName())
                .plan(mapToResponse(subscription.getPlan()))
                .status(subscription.getStatus())
                .billingCycle(subscription.getBillingCycle())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .currentUsers(currentUsers)
                .maxUsers(subscription.getPlan().getMaxUsers())
                .storageUsedMb(storageUsedBytes / (1024 * 1024))
                .maxStorageMb(subscription.getPlan().getMaxStorageMb())
                .build();
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .planName(invoice.getSubscription().getPlan().getName())
                .issuedAt(invoice.getIssuedAt())
                .build();
    }

    private SubscriptionPlan getPlanByName(String name) {
        return planRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan not found: " + name));
    }

    private Subscription getOrCreateActiveSubscription(Organization org) {
        return subscriptionRepository
                .findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(org.getId(), "ACTIVE")
                .orElseGet(() -> {
                    SubscriptionPlan freePlan = getPlanByName("FREE");
                    Subscription subscription = Subscription.builder()
                            .organization(org)
                            .plan(freePlan)
                            .status("ACTIVE")
                            .billingCycle("MONTHLY")
                            .startDate(LocalDateTime.now())
                            .build();
                    return subscriptionRepository.save(subscription);
                });
    }

    private String generateInvoiceNumber() {
        return "INV-" + Year.now().getValue() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public List<PlanResponse> getPlans() {
        return planRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionResponse getSubscription(Long organizationId) {
        Organization org = requireManagedOrganization(organizationId);
        return mapToResponse(getOrCreateActiveSubscription(org));
    }

    @Override
    public List<InvoiceResponse> getInvoices(Long organizationId) {
        requireManagedOrganization(organizationId);
        return invoiceRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionResponse upgradePlan(UpgradePlanRequest request) {
        Organization org = requireManagedOrganization(request.getOrganizationId());
        SubscriptionPlan newPlan = getPlanByName(request.getPlanName());

        Subscription current = getOrCreateActiveSubscription(org);
        if (current.getPlan().getId().equals(newPlan.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Organization is already on the " + newPlan.getName() + " plan");
        }

        // Enforce user limit when downgrading
        long currentUsers = memberRepository.countByOrganizationId(org.getId());
        if (newPlan.getMaxUsers() > 0 && currentUsers > newPlan.getMaxUsers()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot switch to " + newPlan.getName() + ": organization has " + currentUsers
                            + " members but the plan allows only " + newPlan.getMaxUsers());
        }

        current.setStatus("CANCELLED");
        current.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(current);

        String billingCycle = request.getBillingCycle() != null ? request.getBillingCycle() : "MONTHLY";
        Subscription subscription = Subscription.builder()
                .organization(org)
                .plan(newPlan)
                .status("ACTIVE")
                .billingCycle(billingCycle)
                .startDate(LocalDateTime.now())
                .endDate("YEARLY".equals(billingCycle)
                        ? LocalDateTime.now().plusYears(1)
                        : LocalDateTime.now().plusMonths(1))
                .build();
        subscription = subscriptionRepository.save(subscription);

        // Issue invoice + mock payment for paid plans
        BigDecimal amount = "YEARLY".equals(billingCycle) ? newPlan.getYearlyPrice() : newPlan.getMonthlyPrice();
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            boolean loyaltyEligible = isLoyaltyDiscountEligible(org.getId());
            boolean firstYearEligible = "YEARLY".equals(billingCycle) && isFirstYearDiscountEligible(org.getId());

            BigDecimal discountFactor = BigDecimal.ONE;
            if (loyaltyEligible && firstYearEligible) {
                discountFactor = new BigDecimal("0.70"); // 30% discount
            } else if (firstYearEligible) {
                discountFactor = new BigDecimal("0.80"); // 20% discount
            } else if (loyaltyEligible) {
                discountFactor = new BigDecimal("0.90"); // 10% discount
            }
            amount = amount.multiply(discountFactor).setScale(2, java.math.RoundingMode.HALF_UP);

            Invoice invoice = Invoice.builder()
                    .subscription(subscription)
                    .amount(amount)
                    .invoiceNumber(generateInvoiceNumber())
                    .status("PAID")
                    .build();
            invoice = invoiceRepository.save(invoice);

            Payment payment = Payment.builder()
                    .invoice(invoice)
                    .amount(amount)
                    .paymentProvider(request.getPaymentProvider() != null ? request.getPaymentProvider() : "MOCK")
                    .status("SUCCESS")
                    .build();
            paymentRepository.save(payment);
        }

        org.setPlanType(newPlan.getName());
        organizationRepository.save(org);

        return mapToResponse(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscription(Long organizationId) {
        Organization org = requireManagedOrganization(organizationId);

        Subscription current = getOrCreateActiveSubscription(org);
        if ("FREE".equals(current.getPlan().getName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Free plan cannot be cancelled");
        }

        current.setStatus("CANCELLED");
        current.setEndDate(LocalDateTime.now());
        subscriptionRepository.save(current);

        // Revert to FREE plan
        SubscriptionPlan freePlan = getPlanByName("FREE");
        Subscription freeSubscription = Subscription.builder()
                .organization(org)
                .plan(freePlan)
                .status("ACTIVE")
                .billingCycle("MONTHLY")
                .startDate(LocalDateTime.now())
                .build();
        freeSubscription = subscriptionRepository.save(freeSubscription);

        org.setPlanType("FREE");
        organizationRepository.save(org);

        return mapToResponse(freeSubscription);
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountEligibilityResponse getDiscountEligibility(Long organizationId) {
        requireManagedOrganization(organizationId);
        boolean loyaltyEligible = isLoyaltyDiscountEligible(organizationId);
        boolean firstYearEligible = isFirstYearDiscountEligible(organizationId);
        return DiscountEligibilityResponse.builder()
                .loyaltyDiscountEligible(loyaltyEligible)
                .firstYearDiscountEligible(firstYearEligible)
                .build();
    }

    private boolean isLoyaltyDiscountEligible(Long organizationId) {
        List<Subscription> subscriptions = subscriptionRepository.findByOrganizationId(organizationId);
        long totalPaidDays = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Subscription sub : subscriptions) {
            String planName = sub.getPlan().getName();
            if ("PRO".equals(planName) || "BUSINESS".equals(planName) || "ENTERPRISE".equals(planName)) {
                LocalDateTime start = sub.getStartDate();
                LocalDateTime end = sub.getEndDate();
                if (end == null || end.isAfter(now)) {
                    end = now;
                }
                if (end.isAfter(start)) {
                    totalPaidDays += java.time.temporal.ChronoUnit.DAYS.between(start, end);
                }
            }
        }
        return totalPaidDays >= 240; // More than 8 months (~240 days)
    }

    private boolean isFirstYearDiscountEligible(Long organizationId) {
        List<Subscription> subscriptions = subscriptionRepository.findByOrganizationId(organizationId);
        for (Subscription sub : subscriptions) {
            if ("YEARLY".equals(sub.getBillingCycle())) {
                return false;
            }
        }
        return true;
    }
}
