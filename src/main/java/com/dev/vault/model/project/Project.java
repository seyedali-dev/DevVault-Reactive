package com.dev.vault.model.project;

import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project {
    @Id
    private Long projectId;

    private String projectName;
    private String description;
    private LocalDate createdAt;
    private LocalTime creationTime;
    private int memberCount;

    /* relationships */
    @Transient
    private User leader;

    @Transient
    private List<Task> tasks = new ArrayList<>();
    /* end of relationships */

    public void incrementMemberCount() {
        this.memberCount++;
    }
}
