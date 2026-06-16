package com.vortexadmin.service;

import com.vortexadmin.dto.request.TeamRequest;
import com.vortexadmin.dto.response.TeamResponse;

import java.util.List;

public interface TeamService {
    List<TeamResponse> getAllTeams();
    TeamResponse getTeamById(Long id);
    TeamResponse createTeam(TeamRequest request);
    void updateTeam(Long id, TeamRequest request);
    void deleteTeam(Long id);
}
