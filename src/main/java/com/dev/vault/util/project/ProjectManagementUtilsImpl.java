package com.dev.vault.util.project;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.ProjectMembers;
import com.dev.vault.model.entity.project.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.user.UserRole;
import com.dev.vault.model.enums.JoinStatus;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.project.UserProjectRoleReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.repository.user.UserRoleReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.dev.vault.model.enums.JoinStatus.DEFAULT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectManagementUtilsImpl implements ProjectUtils {

    private final UserRoleReactiveRepository userRoleReactiveRepository;
    private final UserProjectRoleReactiveRepository userProjectRoleReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final ModelMapper modelMapper;
    private final ProjectUtilsImpl projectUtils;
    private final UserReactiveRepository userReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersRepository;


    @Override
    public Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user) {
        return null;
    }

    @Override
    public Mono<Boolean> isMemberOfProject(Project project, User user) {
        return null;
    }

    @Override
    public Mono<Boolean> isCouponValid(Project project) {
        return null;
    }

    @Override
    public Mono<Void> performJoinRequestApprovedActions(JoinProjectRequest request) {
        return null;
    }

    @Override
    public Mono<Void> performJoinRequestRejectedActions(JoinProjectRequest request) {
        return null;
    }


    /**
     * Emit the newly created project to the projectSink.
     */
    @Override
    public void emitNewlyCreatedProject(User user, Project project) {
        getUserDtoFlux(project).collectList()
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
     * Check if a {@link Project} with the same name already exists
     */
    @Override
    public Mono<Boolean> checkIfProjectExists(ProjectDto projectDto) {
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
     * Create the {@link Project} object and set the leader to the current user
     */
    @Override
    public Project createProjectObject(ProjectDto projectDto, User currentUser) {
        Project project = modelMapper.map(projectDto, Project.class);
        project.setProjectId(UUID.randomUUID().toString());
        project.setCreatedAt(LocalDateTime.now());
        project.setLeaderEmail(currentUser.getEmail());
        project.incrementMemberCount();
        return project;
    }


    /**
     * Create a new {@link ProjectMembers} object for the current user and save it to the database
     */
    @Override
    public ProjectMembers createProjectMembersObject(User currentUser, Project project) {
        return ProjectMembers.builder()
                .userId(currentUser.getUserId())
                .projectId(project.getProjectId())
                .build();
    }


    /**
     * Create a new {@link UserProjectRole} object for the current user and save it to the database
     */
    @Override
    public Mono<UserProjectRole> createUserProjectRoleObject(User currentUser, Roles projectLeaderRole, Project project) {
        // Check if a `UserProjectRole` object already exists for the user and project
        return userProjectRoleReactiveRepository.findByUserIdAndRoleIdAndProjectId(currentUser.getUserId(), projectLeaderRole.getRoleId(), project.getProjectId())

                // Return the existing `UserProjectRole` object if it exists
                .flatMap(Mono::just)

                // Create a new `UserProjectRole` object if one does not exist
                .switchIfEmpty(Mono.defer(() -> {
                    UserProjectRole userProjectRole = new UserProjectRole(currentUser.getUserId(), projectLeaderRole.getRoleId(), project.getProjectId());
                    return userProjectRoleReactiveRepository.save(userProjectRole);
                }));
    }


    /**
     * Create a new {@link UserRole} object for the current user and save it to the database
     */
    @Override
    public Mono<UserRole> createUserRoleObject(User user, Roles leaderRole) {
        // Check if a UserProjectRole object already exists for the user and project
        return userRoleReactiveRepository.findByUser_UserIdAndRoles_RoleId(user.getUserId(), leaderRole.getRoleId())
                // Create a new `UserRole` object if one does not exist
                .switchIfEmpty(Mono.defer(() -> {
                    UserRole userRole = UserRole.builder()
                            .user(user)
                            .roles(leaderRole)
                            .build();
                    return userRoleReactiveRepository.save(userRole);
                }));
    }

    /**
     * Returns a list of UserDto objects for a given project.
     *
     * @param project The project to get the list of members for.
     * @return A list of UserDto objects representing the members of the project.
     */
    @Override
    public Flux<UserDto> getUserDtoFlux(Project project) {
        // Get all project members associated with the given project
        return projectMembersRepository.findByProjectId(project.getProjectId())
                .flatMap(members -> {
                    // Create a list of UserDto objects for the project members
                    return userReactiveRepository.findById(members.getUserId())
                            .map(user -> UserDto.builder()
                                    .username(user.getUsername())
                                    .major(user.getMajor())
                                    .education(user.getEducation())
                                    .role(user.getUserRoles().stream()
                                            .map(userRole -> userRole.getRoles().getRole().name())
                                            .toList()
                                    ).joinStatus(DEFAULT)
                                    .build()
                            );
                });
    }

}
