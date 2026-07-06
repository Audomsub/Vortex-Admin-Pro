package com.vortexadmin.service;

import com.vortexadmin.dto.request.TaskRequest;
import com.vortexadmin.dto.response.TaskResponse;

import java.util.List;

/**
 * Service contract for task management operations including CRUD, team-scoped retrieval,
 * and assignee-scoped retrieval.
 */
public interface TaskService {

    /**
     * Returns all tasks in the system.
     *
     * @return a list of all task responses
     */
    List<TaskResponse> getAllTasks();

    /**
     * Returns all tasks assigned to the specified team.
     *
     * @param teamId the primary key of the team
     * @return a list of tasks belonging to the given team
     */
    List<TaskResponse> getTasksByTeam(Long teamId);

    /**
     * Returns all tasks assigned to the specified user.
     *
     * @param userId the primary key of the assignee user
     * @return a list of tasks assigned to the given user
     */
    List<TaskResponse> getTasksByAssignee(Long userId);

    /**
     * Returns a single task by its primary key.
     *
     * @param id the primary key of the task to retrieve
     * @return the matching task response
     * @throws com.vortexadmin.exception.ApiException if no task with the given ID exists
     */
    TaskResponse getTaskById(Long id);

    /**
     * Creates a new task using the provided details and returns the persisted task response.
     *
     * @param request the task creation payload including title, description, assignee, and team
     * @return the newly created task response
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Updates an existing task with the provided data.
     *
     * @param id      the primary key of the task to update
     * @param request the updated task data
     * @throws com.vortexadmin.exception.ApiException if the task is not found
     */
    void updateTask(Long id, TaskRequest request);

    /**
     * Deletes the specified task and all of its associated comments.
     *
     * @param id the primary key of the task to delete
     * @throws com.vortexadmin.exception.ApiException if the task is not found
     */
    void deleteTask(Long id);
}
