package com.vortexadmin.controller;

import com.vortexadmin.dto.request.UpgradePlanRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.DiscountEligibilityResponse;
import com.vortexadmin.dto.response.InvoiceResponse;
import com.vortexadmin.dto.response.PlanResponse;
import com.vortexadmin.dto.response.SubscriptionResponse;
import com.vortexadmin.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles HTTP requests for billing and subscription management, including plan listing,
 * subscription changes, invoice retrieval, and discount checks, delegating to BillingService.
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * Retrieves all available subscription plans offered by the platform.
     *
     * @return a list of {@link PlanResponse} objects describing each plan's features and pricing
     */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success("Plans fetched successfully", billingService.getPlans()));
    }

    /**
     * Retrieves the current subscription details for a specific organization.
     *
     * @param organizationId the unique ID of the organization whose subscription is being requested
     * @return the {@link SubscriptionResponse} showing the active plan, status, and renewal date
     */
    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Subscription fetched successfully", billingService.getSubscription(organizationId)));
    }

    /**
     * Retrieves the billing invoice history for a specific organization.
     *
     * @param organizationId the unique ID of the organization whose invoices are being requested
     * @return a list of {@link InvoiceResponse} objects representing past billing invoices
     */
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched successfully", billingService.getInvoices(organizationId)));
    }

    /**
     * Upgrades or downgrades the subscription plan for an organization.
     *
     * @param request the upgrade/downgrade payload containing the target plan ID and organization ID
     * @return the updated {@link SubscriptionResponse} reflecting the new plan
     */
    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradePlan(@Valid @RequestBody UpgradePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan changed successfully", billingService.upgradePlan(request)));
    }

    /**
     * Cancels the active subscription for a specific organization at the end of the current billing period.
     *
     * @param organizationId the unique ID of the organization whose subscription should be cancelled
     * @return the updated {@link SubscriptionResponse} reflecting the cancellation status
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", billingService.cancelSubscription(organizationId)));
    }

    /**
     * Checks whether a specific organization is eligible for any available discounts.
     *
     * @param organizationId the unique ID of the organization to evaluate for discount eligibility
     * @return a {@link DiscountEligibilityResponse} indicating applicable discounts and their details
     */
    @GetMapping("/discounts")
    public ResponseEntity<ApiResponse<DiscountEligibilityResponse>> getDiscounts(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Discount eligibility fetched successfully", billingService.getDiscountEligibility(organizationId)));
    }
}
