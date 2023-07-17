package com.dev.vault.model.entity.task;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.enums.TaskPriority;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Task {

    @Id
    private String taskId;

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
