package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.mappings.ProjectMembers;
import com.dev.vault.model.entity.mappings.UserProjectRole;
import com.dev.vault.model.entity.mappings.UserRole;
import com.dev.vault.repository.mappings.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.service.interfaces.project.ProjectManagementService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.ProjectManagementUtilsImpl;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.dev.vault.model.enums.Role.PROJECT_LEADER;

/**
 * Service Implementation of Project Creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final ProjectMembersReactiveRepository projectMembersReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectManagementUtilsImpl projectManagementUtils;


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
        return projectManagementUtils.checkIfProjectExists(projectDto).flatMap(projectExists ->
                // Get the current user
                authenticationService.getCurrentUserMono().flatMap(currentUser ->
                        // Get the PROJECT_LEADER role
                        reactiveRepositoryUtils.findRoleByRole_OrElseThrow_ResourceNotFoundException(PROJECT_LEADER).flatMap(projectLeaderRole -> {
                            // Create the `Project` object and set the leader to the current user
                            Project project = projectManagementUtils.createProjectObject(projectDto, currentUser);

                            // Create a new `ProjectMembers` object for the current user and save it to the database
                            ProjectMembers projectMembers = projectManagementUtils.createProjectMembersObject(currentUser, project);

                            // Create a new `UserProjectRole` object for the current user and save it to the database
                            Mono<UserProjectRole> userProjectRoleMono = projectManagementUtils.createUserProjectRoleObject(currentUser, projectLeaderRole, project);

                            // Create a new `UserRole` object for the current user and save it to the database
                            Mono<UserRole> userRoleMono = projectManagementUtils.createUserRoleObject(currentUser, projectLeaderRole);

                            Mono<Project> savedProjectMono = projectReactiveRepository.save(project);
                            Mono<ProjectMembers> savedProjectMembersMono = projectMembersReactiveRepository.save(projectMembers);

                            // Emit the newly created project to the projectSink
                            projectManagementUtils.emitNewlyCreatedProject(currentUser, project);

                            // combine all asynchronous operations and Return a ProjectDto object with the project information
                            return Mono.zip(savedProjectMono, savedProjectMembersMono, userProjectRoleMono, userRoleMono)
                                    .map(tuple ->
                                            ProjectDto.builder()
                                                    .projectName(project.getProjectName())
                                                    .projectDescription(project.getDescription())
                                                    .build()
                                    );
                        })
                )
        );
    }


    /**
     * Get all the members of a project.
     *
     * @param projectId id of the project that you want to see the members of.
     * @return Member of project as Flux of {@link ProjectMembersDto} which contains a list of {@link UserDto}.
     */
    @Override
    public Flux<ProjectMembersDto> getAllMembersOfProject(String projectId) {
        // find the project first
        Mono<Project> projectMono = reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNotFoundException(projectId);
        return projectMono.flatMapMany(project -> {

            // then build the `UserDto` list from the project (get the members of the found project)
            Mono<List<UserDto>> userDtoMonoList = projectManagementUtils.getUserDtoFlux(project).collectList();

            // create and return a `ProjectMembersDto` with the `UserDto` list
            return Flux.zip(userDtoMonoList, projectMono)
                    .map(tuple ->
                            ProjectMembersDto.builder()
                                    .projectMembers(tuple.getT1())
                                    .build()
                    );
        });
    }

}
