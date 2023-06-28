package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.group.ProjectDto;

public interface ProjectManagementService {
    ProjectDto createProject(ProjectDto projectDto);
}
