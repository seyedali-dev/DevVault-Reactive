package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;

public interface ProjectManagementService {
    ProjectDto createProject(ProjectDto projectDto);
}
