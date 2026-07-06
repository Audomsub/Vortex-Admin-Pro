package com.vortexadmin.service;

import com.vortexadmin.dto.request.TeamRequest;
import com.vortexadmin.dto.response.TeamResponse;

import java.util.List;

/**
 * Service contract for team management operations including full CRUD for organizational teams.
 */
public interface TeamService {

    /**
     * Returns all teams in the system.
     *
     * @return a list of all team responses
     */
    List<TeamResponse> getAllTeams();

    /**
     * Returns a single team by its primary key.
     *
     * @param id the primary key of the team to retrieve
     * @return the matching team response
     * @throws com.vortexadmin.exception.ApiException if no team with the given ID exists
     */
    TeamResponse getTeamById(Long id);

    /**
     * Creates a new team with the provided details and returns the persisted team response.
     *
     * @param request the team creation payload including name and description
     * @return the newly created team response
     */
    TeamResponse createTeam(TeamRequest request);

    /**
     * Updates an existing team with the provided data.
     *
     * @param id      the primary key of the team to update
     * @param request the updated team data
     * @throws com.vortexadmin.exception.ApiException if the team is not found
     */
    void updateTeam(Long id, TeamRequest request);

    /**
     * Deletes the specified team.
     *
     * @param id the primary key of the team to delete
     * @throws com.vortexadmin.exception.ApiException if the team is not found
     */
    void deleteTeam(Long id);
}
