package com.dev.vault.util.project;

import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.mappings.ProjectMembers;
import com.dev.vault.model.entity.mappings.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.mappings.UserRole;
import com.dev.vault.model.enums.Role;
import com.dev.vault.repository.mappings.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.mappings.UserProjectRoleReactiveRepository;
import com.dev.vault.repository.user.RolesReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Service implementation for ProjectUtils.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class ProjectUtilsImpl implements ProjectUtils {

    public final Sinks.Many<SearchResponse> projectSink = Sinks.many().replay().all();

    private final ProjectMembersReactiveRepository projectMembersReactiveRepository;
    private final UserProjectRoleReactiveRepository userProjectRoleReactiveRepository;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final UserReactiveRepository userReactiveRepository;
    private final RolesReactiveRepository rolesReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;


    /**
     * Checks if the user is the leader or admin of the project
     *
     * @param project the project to check for leadership or admin role
     * @param user    the user to check for leadership or admin role
     * @return Mono of true if the user is the leader or admin of the project, false otherwise
     */
    @Override
    public Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user) {
        // Find the user's role in the project
        return reactiveRepositoryUtils.findAllUserRolesByUserId_OrElseThrow_ResourceNotFoundException(user.getUserId())
                .flatMap(userRole -> Mono.just(userRole.getRoles()))
                .filter(role ->
                        role.getRole().equals(Role.PROJECT_LEADER) || role.getRole().equals(Role.PROJECT_ADMIN)
                )
                // Find the user's role for the specified project
                .flatMap(roles ->
                        userProjectRoleReactiveRepository.findByUserIdAndRoleIdAndProjectId(user.getUserId(), roles.getRoleId(), project.getProjectId())
                )
                // Fetch the corresponding User, Role, and Project entities from the database
                .flatMap(userProjectRole -> {
                    Mono<User> userMono = userReactiveRepository.findById(userProjectRole.getUserId());
                    Mono<Roles> rolesMono = rolesReactiveRepository.findById(userProjectRole.getRoleId());
                    Mono<Project> projectMono = projectReactiveRepository.findById(userProjectRole.getProjectId());

                    return Mono.zip(userMono, rolesMono, projectMono)
                            .flatMap(tuple -> {
                                User fetchedUser = tuple.getT1();
                                Roles fetchedRole = tuple.getT2();
                                Project fetchedProject = tuple.getT3();

                                // Return true if the user has the leader or admin role in the project, false otherwise
                                if (fetchedRole.getRole() == Role.PROJECT_LEADER || fetchedRole.getRole() == Role.PROJECT_ADMIN)
                                    return Mono.just(true);
                                else
                                    return Mono.just(false);
                            });
                }).defaultIfEmpty(false)
                .any(Boolean::booleanValue);
    }


    /**
     * Checks if the user is a member of the project
     *
     * @param project the project to check for membership
     * @param user    the user to check for membership
     * @return <code>true</code> if the user is a member of the project, false otherwise
     */
    @Override
    public Mono<Boolean> isMemberOfProject(Project project, User user) {
        return projectMembersReactiveRepository.findByProjectIdAndUserId(project.getProjectId(), user.getUserId())
                .hasElement()
                .map(foundMembers -> foundMembers);
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

    @Override
    public void emitNewlyCreatedProject(User user, Project project) {

    }

    @Override
    public Mono<Boolean> checkIfProjectExists(ProjectDto projectDto) {
        return null;
    }

    @Override
    public Project createProjectObject(ProjectDto projectDto, User currentUser) {
        return null;
    }

    @Override
    public ProjectMembers createProjectMembersObject(User currentUser, Project project) {
        return null;
    }

    @Override
    public Mono<UserProjectRole> createUserProjectRoleObject(User currentUser, Roles projectLeaderRole, Project project) {
        return null;
    }

    @Override
    public Mono<UserRole> createUserRoleObject(User user, Roles leaderRole) {
        return null;
    }

    @Override
    public Flux<UserDto> getUserDtoFlux(Project project) {
        return null;
    }

}

