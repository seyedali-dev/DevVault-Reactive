package com.dev.vault.service.interfaces.task;

import com.dev.vault.model.enums.TaskStatus;

public interface TaskProgressService {

    void markTaskAsCompleted(Long taskId, Long projectId, TaskStatus taskStatus);

}
