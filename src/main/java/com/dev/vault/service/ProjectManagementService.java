package com.dev.vault.service;

import com.dev.vault.helper.payload.group.ProjectDto;

public interface ProjectManagementService {
    ProjectDto createProject(ProjectDto projectDto);
}
