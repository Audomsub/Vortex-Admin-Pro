package com.vortexadmin.service;

import com.vortexadmin.dto.request.UpgradePlanRequest;
import com.vortexadmin.dto.response.DiscountEligibilityResponse;
import com.vortexadmin.dto.response.InvoiceResponse;
import com.vortexadmin.dto.response.PlanResponse;
import com.vortexadmin.dto.response.SubscriptionResponse;

import java.util.List;

/**
 * Service contract for billing and subscription management, covering plan listing, subscription
 * retrieval, invoice history, plan upgrades, cancellation, and discount eligibility checks.
 */
public interface BillingService {

    /**
     * Returns all available subscription plans offered by the platform.
     *
     * @return a list of plan responses describing each available tier
     */
    List<PlanResponse> getPlans();

    /**
     * Returns the active subscription for the specified organization.
     *
     * @param organizationId the primary key of the organization
     * @return the current subscription response for the given organization
     * @throws com.vortexadmin.exception.ApiException if no active subscription is found
     */
    SubscriptionResponse getSubscription(Long organizationId);

    /**
     * Returns the invoice history for the specified organization, ordered from newest to oldest.
     *
     * @param organizationId the primary key of the organization
     * @return a list of invoice responses for the given organization
     */
    List<InvoiceResponse> getInvoices(Long organizationId);

    /**
     * Upgrades or changes the subscription plan for the organization specified in the request,
     * generates a new invoice for the prorated amount, and returns the updated subscription.
     *
     * @param request the upgrade payload containing the organization ID and the target plan ID
     * @return the updated subscription response reflecting the new plan
     * @throws com.vortexadmin.exception.ApiException if the organization or plan is not found
     */
    SubscriptionResponse upgradePlan(UpgradePlanRequest request);

    /**
     * Cancels the active subscription for the specified organization by setting its status to
     * {@code CANCELLED}.
     *
     * @param organizationId the primary key of the organization whose subscription should be cancelled
     * @return the updated subscription response with {@code CANCELLED} status
     * @throws com.vortexadmin.exception.ApiException if no active subscription is found
     */
    SubscriptionResponse cancelSubscription(Long organizationId);

    /**
     * Checks whether the specified organization qualifies for a discount based on criteria
     * such as subscription duration or member count.
     *
     * @param organizationId the primary key of the organization to evaluate
     * @return a {@link DiscountEligibilityResponse} indicating eligibility and the applicable
     *         discount percentage if eligible
     */
    DiscountEligibilityResponse getDiscountEligibility(Long organizationId);
}
