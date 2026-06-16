package com.vortexadmin.service.impl;

import com.vortexadmin.dto.request.TeamRequest;
import com.vortexadmin.dto.response.TeamResponse;
import com.vortexadmin.entity.Team;
import com.vortexadmin.exception.ApiException;
import com.vortexadmin.repository.TeamRepository;
import com.vortexadmin.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    private TeamResponse mapToResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .build();
    }

    @Override
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        return mapToResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapToResponse(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));
        teamRepository.delete(team);
    }
}
