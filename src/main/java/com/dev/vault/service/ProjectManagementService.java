package com.dev.vault.service;

import com.dev.vault.helper.payload.group.ProjectDto;

public interface ProjectManagementService {
    ProjectDto createProjectOrGroup(ProjectDto projectDto);
}
