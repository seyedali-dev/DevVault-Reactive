package com.dev.vault.helper.payload.group;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDto {
    private String projectName;
    private String projectDescription;
    private LocalDate createdAt;
    private LocalTime creationTime;
}
