package com.vortexadmin.controller;

import com.vortexadmin.dto.request.UpgradePlanRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.InvoiceResponse;
import com.vortexadmin.dto.response.PlanResponse;
import com.vortexadmin.dto.response.SubscriptionResponse;
import com.vortexadmin.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.success("Plans fetched successfully", billingService.getPlans()));
    }

    @GetMapping("/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Subscription fetched successfully", billingService.getSubscription(organizationId)));
    }

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Invoices fetched successfully", billingService.getInvoices(organizationId)));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradePlan(@Valid @RequestBody UpgradePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Plan changed successfully", billingService.upgradePlan(request)));
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", billingService.cancelSubscription(organizationId)));
    }
}
