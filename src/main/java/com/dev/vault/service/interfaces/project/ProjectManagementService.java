package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;
import reactor.core.publisher.Mono;

public interface ProjectManagementService {
    Mono<ProjectDto> createProject(ProjectDto projectDto);
}
