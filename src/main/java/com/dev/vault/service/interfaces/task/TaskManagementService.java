package com.dev.vault.service.interfaces.task;

import com.dev.vault.helper.payload.request.task.TaskRequest;
import com.dev.vault.helper.payload.response.task.TaskResponse;
import reactor.core.publisher.Mono;

public interface TaskManagementService {
    Mono<TaskResponse> createNewTask(String projectId, TaskRequest taskRequest);
}
