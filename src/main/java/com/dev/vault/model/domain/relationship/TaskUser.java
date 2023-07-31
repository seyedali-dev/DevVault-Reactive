package com.dev.vault.model.domain.relationship;

import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.domain.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Manages the relationship between {@link User} and {@link Task}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class TaskUser {

    @Id
    private String taskUserId;

    /* relationships */
    private User user;
    private Task task;
    /* end of relationships */

}
