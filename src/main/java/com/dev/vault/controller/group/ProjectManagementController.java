package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.dto.ProjectDto;
import com.dev.vault.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proj_leader")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN')")
public class ProjectManagementController {
    private final ProjectManagementService projectManagementService;

    @PreAuthorize("hasAnyAuthority('team_member:create_project','project_leader:create_project','group_admin:create_project')")
    @PostMapping({"/create-project", "/create-group"})
    public ResponseEntity<?> createProjectOrGroup(@RequestBody ProjectDto projectDto) {
        return new ResponseEntity<>(projectManagementService.createProjectOrGroup(projectDto), HttpStatus.CREATED);
    }
}
