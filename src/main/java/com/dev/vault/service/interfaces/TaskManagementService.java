package com.dev.vault.service.interfaces;

import com.dev.vault.helper.payload.task.TaskRequest;
import com.dev.vault.helper.payload.task.TaskResponse;

import java.util.List;

public interface TaskManagementService {
    TaskResponse createNewTask(Long projectId, TaskRequest taskRequest);
}
