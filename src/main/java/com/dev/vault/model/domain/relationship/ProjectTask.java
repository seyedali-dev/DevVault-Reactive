package com.dev.vault.model.domain.relationship;

import com.dev.vault.model.domain.project.Project;
import com.dev.vault.model.domain.task.Task;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Manages the relationship between {@link Project} and {@link Task}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class ProjectTask {

    @Id
    private String projectTaskId;

    /* relationships */
    private Project project;
    private Task task;
    /* end of relationships */

}
