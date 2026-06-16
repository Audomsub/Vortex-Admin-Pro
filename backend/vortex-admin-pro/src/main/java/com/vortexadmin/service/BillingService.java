package com.vortexadmin.service;

import com.vortexadmin.dto.request.UpgradePlanRequest;
import com.vortexadmin.dto.response.InvoiceResponse;
import com.vortexadmin.dto.response.PlanResponse;
import com.vortexadmin.dto.response.SubscriptionResponse;

import java.util.List;

public interface BillingService {

    List<PlanResponse> getPlans();

    SubscriptionResponse getSubscription(Long organizationId);

    List<InvoiceResponse> getInvoices(Long organizationId);

    SubscriptionResponse upgradePlan(UpgradePlanRequest request);

    SubscriptionResponse cancelSubscription(Long organizationId);
}
