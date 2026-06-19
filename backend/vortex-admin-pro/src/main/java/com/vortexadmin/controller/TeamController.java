package com.vortexadmin.controller;

import com.vortexadmin.dto.request.TeamRequest;
import com.vortexadmin.dto.response.ApiResponse;
import com.vortexadmin.dto.response.TeamResponse;
import com.vortexadmin.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAuthority('team.read')")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success("Teams fetched successfully", teamService.getAllTeams()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('team.read')")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Team fetched successfully", teamService.getTeamById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('team.create')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team created successfully", teamService.createTeam(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('team.update')")
    public ResponseEntity<ApiResponse<Void>> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('team.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
