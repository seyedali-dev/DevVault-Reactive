package com.dev.vault.helper.payload.task;

import com.dev.vault.model.task.enums.TaskPriority;
import com.dev.vault.model.task.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TaskRequest {
    private String taskName;
    @FutureOrPresent
    private String description;
    private LocalDateTime dueDate;
    private TaskStatus taskStatus;
    private TaskPriority taskPriority;
}
