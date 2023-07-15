package com.dev.vault.model.comment;

import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    @Id
    private Long commentId;

    private String comment;
    private LocalDateTime commentedAt;

    /* relationships */
    @Transient
    private Project commentedOnProject;

    @Transient
    private Task commentedOnTask;

    @Transient
    private User commentedBy;
    /* end of relationships */
}
