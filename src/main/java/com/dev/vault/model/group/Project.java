package com.dev.vault.model.group;

import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;

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

    /* relationships */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private User leader;
    /* end of relationships */

}
