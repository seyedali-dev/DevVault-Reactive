package com.dev.vault.controller.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.service.interfaces.project.ProjectManagementService;
import com.dev.vault.service.interfaces.project.SearchProjectService;
import com.dev.vault.util.project.ProjectUtilsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * REST controller for managing projects and groups.
 */
@RestController
@RequestMapping("/api/v1/proj_leader")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;
    private final ProjectUtilsImpl projectUtils;
    private final SearchProjectService searchProjectService;

    /**
     * Endpoint for creating a new project.
     *
     * @param projectDto the project details to create
     * @return a ResponseEntity containing the created project and an HTTP status code of 201 (CREATED)
     */
//    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'PROJECT_ADMIN')")
    /*@PostMapping({"/create-project", "/create-project"})
    public Mono<ResponseEntity<ProjectDto>> createProject(@RequestBody ProjectDto projectDto) {
        return projectManagementService.createProject(projectDto)
                .doOnNext(savedProjectDto -> searchProjectService.listAllProjects()
                        .map(projectUtils.projectSink::tryEmitNext)
                )
                .map(savedProjectDto -> ResponseEntity.status(CREATED).body(savedProjectDto));
    }*/
    @PostMapping({"/create-project", "/create-project"})
    public Mono<ResponseEntity<ProjectDto>> createProject(@RequestBody ProjectDto projectDto) {
        return projectManagementService.createProject(projectDto)
                .map(savedProjectDto -> ResponseEntity.status(CREATED).body(savedProjectDto));
    }

}
