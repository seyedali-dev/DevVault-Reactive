package com.dev.vault.model.group;

import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    private String projectName;
    private String description;
    @CreationTimestamp
    private LocalDate createdAt;
    @CreationTimestamp
    private LocalTime creationTime;
    private int memberCount;

    /* relationships */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private User leader;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
    private List<Task> tasks = new ArrayList<>();
    /* end of relationships */

    public void incrementMemberCount() {
        this.memberCount++;
    }
}
