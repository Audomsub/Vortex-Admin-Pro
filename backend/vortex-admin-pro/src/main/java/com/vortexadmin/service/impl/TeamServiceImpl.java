package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.TeamRequest;
import com.vortexadmin.dto.response.TeamResponse;
import com.vortexadmin.entity.Team;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.TeamRepository;
import com.vortexadmin.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles team management business logic including creation, retrieval, update,
 * and deletion of teams used to group tasks and users within the platform.
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    /**
     * Maps a {@link Team} entity to a {@link TeamResponse} DTO.
     *
     * @param team the team entity to map
     * @return the corresponding team response DTO
     */
    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .build();
    }

    /**
     * Returns all teams in the system.
     *
     * @return a list of team response DTOs for every team
     */
    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the team identified by the given id.
     *
     * @param id the id of the team to retrieve
     * @return the team response DTO for the requested team
     * @throws ApiException with {@code 404} if no team with that id exists
     */
    @Override
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        return mapToResponse(team);
    }

    /**
     * Creates and persists a new team with the given name and description.
     *
     * @param request the creation request containing the team name and description
     * @return the newly created team as a {@link TeamResponse} DTO
     */
    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToResponse(teamRepository.save(team));
    }

    /**
     * Updates the name and description of an existing team.
     *
     * @param id      the id of the team to update
     * @param request the update payload containing the new name and description
     * @throws ApiException with {@code 404} if no team with that id exists
     */
    @Override
    @Transactional
    public void updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));

        team.setName(request.getName());
        team.setDescription(request.getDescription());

        teamRepository.save(team);
    }

    /**
     * Permanently deletes the team identified by the given id.
     *
     * @param id the id of the team to delete
     * @throws ApiException with {@code 404} if no team with that id exists
     */
    @Override
    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        teamRepository.delete(team);
    }
}
