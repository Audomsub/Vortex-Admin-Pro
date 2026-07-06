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

/**
 * Handles HTTP requests for team management operations, including creating, retrieving,
 * updating, and deleting teams, delegating all business logic to TeamService.
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Retrieves all teams within the authenticated user's tenant.
     *
     * @return a list of {@link TeamResponse} objects representing all teams
     */
    @GetMapping
    @PreAuthorize("hasAuthority('team.read')")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success("Teams fetched successfully", teamService.getAllTeams()));
    }

    /**
     * Retrieves a single team by its unique identifier.
     *
     * @param id the unique ID of the team to retrieve
     * @return the {@link TeamResponse} for the specified team
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('team.read')")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Team fetched successfully", teamService.getTeamById(id)));
    }

    /**
     * Creates a new team with the provided details.
     *
     * @param request the team creation payload containing the team name and member IDs
     * @return the created {@link TeamResponse} reflecting the persisted team
     */
    @PostMapping
    @PreAuthorize("hasAuthority('team.create')")
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team created successfully", teamService.createTeam(request)));
    }

    /**
     * Updates an existing team's details by its unique identifier.
     *
     * @param id      the unique ID of the team to update
     * @param request the update payload containing the new team name and/or member IDs
     * @return a success response with no data payload upon successful update
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('team.update')")
    public ResponseEntity<ApiResponse<Void>> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", null));
    }

    /**
     * Deletes a team by its unique identifier.
     *
     * @param id the unique ID of the team to delete
     * @return a success response with no data payload upon successful deletion
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('team.delete')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }
}
