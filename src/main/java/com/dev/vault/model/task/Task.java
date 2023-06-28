package com.dev.vault.model.task;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.enums.TaskPriority;
import com.dev.vault.model.task.enums.TaskStatus;
import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    private String taskName;
    private String description;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime completionDate;
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    @Enumerated(EnumType.STRING)
    private TaskPriority taskPriority;
    private boolean hasOverdue;

    /* relationships */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "tasks_users",
            joinColumns = @JoinColumn(name = "task_Id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedUsers = new HashSet<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;
    /* end of relationships */
}
