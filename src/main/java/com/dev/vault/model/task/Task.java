package com.dev.vault.model.task;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.enums.TaskPriority;
import com.dev.vault.model.task.enums.TaskStatus;
import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    @Id
    private Long taskId;

    private String taskName;
    private String description;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime completionDate;
    private TaskStatus taskStatus;
    private TaskPriority taskPriority;
    private boolean hasOverdue;

    /* relationships */
    @Transient
    private Set<User> assignedUsers = new HashSet<>();

    @Transient
    private User createdBy;

    @Transient
    private Project project;
    /* end of relationships */
}
