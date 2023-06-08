package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.group.ProjectDto;
import com.dev.vault.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proj_leader")
@RequiredArgsConstructor
public class ProjectManagementController {
    private final ProjectManagementService projectManagementService;

    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN')")
    @PostMapping({"/create-project", "/create-group"})
    public ResponseEntity<?> createProjectOrGroup(@RequestBody ProjectDto projectDto) {
        return new ResponseEntity<>(projectManagementService.createProject(projectDto), HttpStatus.CREATED);
    }
}
