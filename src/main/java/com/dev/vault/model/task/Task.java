package com.dev.vault.model.task;

import com.dev.vault.model.group.Project;
import com.dev.vault.model.task.enums.TaskPriority;
import com.dev.vault.model.task.enums.TaskStatus;
import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @FutureOrPresent
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;
    @Enumerated(EnumType.STRING)
    private TaskPriority taskPriority;

    /* relationships */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tasks_users",
            joinColumns = @JoinColumn(name = "task_Id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> assignedTo = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;
    /* end of relationships */
}
