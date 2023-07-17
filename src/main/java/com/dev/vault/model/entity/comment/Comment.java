package com.dev.vault.model.entity.comment;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Comment {

    @Id
    private String commentId;

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
