package com.vortexadmin.controller;

import com.vortexadmin.dto.request.AcceptInvitationRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.InvitationResponse;
import com.vortexadmin.dto.response.OrganizationResponse;
import com.vortexadmin.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final OrganizationService organizationService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyPendingInvitations() {
        return ResponseEntity.ok(ApiResponse.success("Pending invitations fetched successfully", organizationService.getMyPendingInvitations()));
    }

    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<OrganizationResponse>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", organizationService.acceptInvitation(request.getToken())));
    }
}
