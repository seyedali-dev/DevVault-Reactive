package com.dev.vault.model.entity.mappings;

import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
