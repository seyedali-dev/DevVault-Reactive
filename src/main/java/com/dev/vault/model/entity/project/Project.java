package com.dev.vault.model.entity.project;

import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Project {

    @Id
    private String projectId;

    private String projectName;
    private String description;
    @CreatedDate
    private LocalDateTime createdAt;
    private int memberCount;

    /* relationships */
    @Transient
    @CreatedBy
    private User leader;

    @Transient
    private List<Task> tasks = new ArrayList<>();
    /* end of relationships */

    public void incrementMemberCount() {
        this.memberCount++;
    }
}
