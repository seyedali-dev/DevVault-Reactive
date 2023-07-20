package com.dev.vault.helper.payload.request.project;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDto {
    private String projectName;
    private String projectDescription;
}
