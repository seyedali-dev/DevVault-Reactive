package com.dev.vault.helper.payload.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
