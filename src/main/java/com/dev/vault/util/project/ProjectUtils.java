package com.dev.vault.util.project;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.ProjectMembers;
import com.dev.vault.model.entity.project.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.user.UserRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectUtils {

    /**
     * Checks if the user is the leader or admin of the project
     *
     * @param project the project to check for leadership or admin role
     * @param user    the user to check for leadership or admin role
     * @return Mono of true if the user is the leader or admin of the project, false otherwise
     */
    Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user);

    /**
     * Checks if the user is already a member of the specified project or has already sent a join project request for the project.
     *
     * @param project the project to check for membership
     * @param user    the user to check for membership
     * @return <code>Mono of true</code> if the user is already a member of the project or has already sent a join project request for the project, false otherwise
     * @throws ResourceNotFoundException      if the project cannot be found
     * @throws ResourceAlreadyExistsException if the user has already sent a join project request for the project
     */
    Mono<Boolean> isMemberOfProject(Project project, User user);

    /**
     * Checks if the JoinRequestCoupon is valid for the specified project.
     *
     * @param project the project to check the JoinRequestCoupon for
     * @return <code>Mono.just(true)</code> if the JoinRequestCoupon is valid, false otherwise
     * @throws DevVaultException         if the JoinRequestCoupon has been used or has exceeded its maximum usage count
     * @throws ResourceNotFoundException if the JoinRequestCoupon cannot be found
     */
    Mono<Boolean> isCouponValid(Project project);

    /**
     * Performs actions when a join request is approved.
     *
     * @param request The join request that was approved.
     */
    Mono<Void> performJoinRequestApprovedActions(JoinProjectRequest request);

    /**
     * Performs actions when a join request is rejected.
     *
     * @param request The join request that was rejected.
     */
    Mono<Void> performJoinRequestRejectedActions(JoinProjectRequest request);

    /**
     * Emit the newly created project to the projectSink.
     */
    void emitNewlyCreatedProject(User user, Project project);

    /**
     * Check if a {@link Project} with the same name already exists
     */
    Mono<Boolean> checkIfProjectExists(ProjectDto projectDto);

    /**
     * Create the {@link Project} object and set the leader to the current user
     */
    Project createProjectObject(ProjectDto projectDto, User currentUser);

    /**
     * Create a new {@link ProjectMembers} object for the current user and save it to the database
     */
    ProjectMembers createProjectMembersObject(User currentUser, Project project);

    /**
     * Create a new {@link UserProjectRole} object for the current user and save it to the database
     */
    Mono<UserProjectRole> createUserProjectRoleObject(User currentUser, Roles projectLeaderRole, Project project);

    /**
     * Create a new {@link UserRole} object for the current user and save it to the database
     */
    Mono<UserRole> createUserRoleObject(User user, Roles leaderRole);

    /**
     * Returns a list of UserDto objects for a given project.
     *
     * @param project The project to get the list of members for.
     * @return A list of UserDto objects representing the members of the project.
     */
    Flux<UserDto> getUserDtoFlux(Project project);

}
