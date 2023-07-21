package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.ProjectMembers;
import com.dev.vault.model.entity.project.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.enums.Role;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.project.UserProjectRoleReactiveRepository;
import com.dev.vault.service.interfaces.project.ProjectManagementService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectUtilsImpl;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service Implementation of Project Creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final UserProjectRoleReactiveRepository userProjectRoleReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationService authenticationService;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectUtilsImpl projectUtils;
    private final SearchProjectServiceImpl searchProjectServiceImpl;


    /**
     * Creates a new project with the given project details, assigns the current user as the project leader,
     * increments the count of member by one, and saves the project to the database.
     * If a project with the same name already exists, an error is thrown.
     *
     * @param projectDto the DTO containing the project details
     * @return a Mono of ProjectDto containing the project information
     * @throws ResourceAlreadyExistsException if a project with the same name already exists
     */
    @Override
    @Transactional
    public Mono<ProjectDto> createProject(ProjectDto projectDto) {
        // Check if a project with the same name already exists
        return checkIfProjectExists(projectDto).flatMap(projectExists ->
                        // Get the current user
                        authenticationService.getCurrentUserMono().flatMap(currentUser ->
                                        // Get the PROJECT_LEADER role
                                        reactiveRepositoryUtils.findRoleByRole_OrElseThrow_ResourceNotFoundException(Role.PROJECT_LEADER).flatMap(projectLeaderRole -> {
                                            // Create the Project object and set the leader to the current user
                                            Project project = createProjectObject(projectDto, currentUser);

                                            // Create a new ProjectMembers object for the current user and save it to the database
                                            ProjectMembers projectMembers = createProjectMembersObject(currentUser, project);

                                            // Create a new UserProjectRole object for the current user and save it to the database
                                            UserProjectRole userProjectRole = createUserProjectRoleObject(currentUser, projectLeaderRole, project);

                                            Mono<Project> savedProjectMono = projectReactiveRepository.save(project);
                                            Mono<ProjectMembers> savedProjectMembersMono = projectMembersReactiveRepository.save(projectMembers);
                                            Mono<UserProjectRole> savedUserProjectRoleMono = userProjectRoleReactiveRepository.save(userProjectRole);

                                            // Emit the newly created project to the projectSink
                                            emitNewlyCreatedProject(currentUser, project);

                                            // combine all asynchronous operations and Return a ProjectDto object with the project information
                                            return Mono.zip(savedProjectMono, savedProjectMembersMono, savedUserProjectRoleMono)
                                                    .map(tuple -> ProjectDto.builder()
                                                            .projectName(project.getProjectName())
                                                            .projectDescription(project.getDescription())
                                                            .build()
                                                    );
                                        })
                        )
        );
    }

    /**
     * Emit the newly created project to the projectSink.
     */
    private void emitNewlyCreatedProject(User user, Project project) {
        searchProjectServiceImpl.getUserDtoFlux(project).collectList()
                .map(userDtos -> {
                    SearchResponse searchResponse = SearchResponse.builder()
                            .projectId(project.getProjectId())
                            .projectName(project.getProjectName())
                            .projectDescription(project.getDescription())
                            .leaderEmail(user.getUsername())
                            .members(new ProjectMembersDto(userDtos))
                            .build();
                    projectUtils.projectSink.emitNext(searchResponse, Sinks.EmitFailureHandler.FAIL_FAST);
                    log.info("emitting newly created project, projectMembers: {{}}", searchResponse.getMembers().getProjectMembers());
                    return Mono.empty();
                }).subscribe();
    }


    /**
     * Check if a project with the same name already exists
     */
    private Mono<Boolean> checkIfProjectExists(ProjectDto projectDto) {
        return projectReactiveRepository.existsByProjectNameIgnoreCase(projectDto.getProjectName())
                .flatMap(exists -> {
                    if (exists) {
                        log.info("⚠️this project already exists! provide a unique name");
                        return Mono.error(new ResourceAlreadyExistsException("Project", "Project Name", projectDto.getProjectName()));
                    }
                    return Mono.just(false);
                });
    }


    /**
     * Create the Project object and set the leader to the current user
     */
    private Project createProjectObject(ProjectDto projectDto, User currentUser) {
        Project project = modelMapper.map(projectDto, Project.class);
        project.setProjectId(UUID.randomUUID().toString());
        project.setCreatedAt(LocalDateTime.now());
        project.setLeaderEmail(currentUser.getEmail());
        project.incrementMemberCount();
        return project;
    }


    /**
     * Create a new ProjectMembers object for the current user and save it to the database
     */
    private ProjectMembers createProjectMembersObject(User currentUser, Project project) {
        return ProjectMembers.builder()
                .userId(currentUser.getUserId())
                .projectId(project.getProjectId())
                .build();
    }


    /**
     * Create a new UserProjectRole object for the current user and save it to the database
     */
    private UserProjectRole createUserProjectRoleObject(User currentUser, Roles projectLeaderRole, Project project) {
        return UserProjectRole.builder()
                .userId(currentUser.getUserId())
                .roleId(projectLeaderRole.getRoleId())
                .projectId(project.getProjectId())
                .build();
    }

}
