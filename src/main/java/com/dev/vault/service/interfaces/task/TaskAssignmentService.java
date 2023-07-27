package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.payload.response.task.TaskResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TaskAssignmentService {

    Mono<TaskResponse> assignTaskToUsers(String taskId, String projectId, List<String> userIdList);

    TaskResponse assignTaskToAllUsersInProject(Long taskId, Long projectId);

    void unAssignTaskFromUser(Long taskId, Long projectId, Long userId);

    void unAssignTaskFromUsers(Long taskId, Long projectId, List<Long> userIdList);

    void unassignTaskFromAllUsersInProject(Long taskId, Long projectId);
}
