package com.dev.vault.helper.payload.task;

import com.dev.vault.model.task.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TaskResponse {
    private String taskName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dueDate;
    private String projectName;
    private TaskStatus taskStatus;
    private Map<String, String> assignedUsers;
}
