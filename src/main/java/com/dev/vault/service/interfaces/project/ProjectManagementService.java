package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectManagementService {
    Mono<ProjectDto> createProject(ProjectDto projectDto);

    Flux<ProjectMembersDto> getAllMembersOfProject(String projectId);
}
