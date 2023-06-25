package com.dev.vault.service.interfaces;

import com.dev.vault.helper.payload.task.TaskResponse;

import java.util.List;

public interface TaskAssignmentService {
    TaskResponse assignTaskToUsers(Long taskId, Long projectId, List<Long> userIdList);

    TaskResponse assignTaskToAllUsersInProject(Long taskId, Long projectId);
}
