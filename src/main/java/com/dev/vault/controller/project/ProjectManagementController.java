package com.dev.vault.controller.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.service.interfaces.project.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * REST controller for managing projects and groups.
 */
@RestController
@RequestMapping("/api/v1/proj_leader")
@RequiredArgsConstructor
public class ProjectManagementController {

    private final ProjectManagementService projectManagementService;


    /**
     * Endpoint for creating a new project.
     *
     * @param projectDto the project details to create
     * @return a ResponseEntity containing the created project and an HTTP status code of 201 (CREATED)
     */
    @PostMapping({"/create-project", "/create-project"})
    public Mono<ResponseEntity<ProjectDto>> createProject(@RequestBody ProjectDto projectDto) {
        return projectManagementService.createProject(projectDto)
                .map(savedProjectDto -> ResponseEntity.status(CREATED).body(savedProjectDto));
    }


    /**
     * Endpoint for getting all the members of a project.
     *
     * @param projectId id of the project that you want to see the members of.
     * @return Member of project as Flux of {@link ProjectMembersDto} which contains a list of {@link UserDto}.
     */
    @GetMapping("/members")
    public Mono<ResponseEntity<Flux<ProjectMembersDto>>> getAllMembersOfProject(@RequestParam String projectId) {
        return Mono.just(
                ResponseEntity.ok(projectManagementService.getAllMembersOfProject(projectId))
        );
    }

}
