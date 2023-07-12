package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;

public interface TaskManagementService {
    TaskResponse createNewTask(Long projectId, TaskRequest taskRequest);
}
