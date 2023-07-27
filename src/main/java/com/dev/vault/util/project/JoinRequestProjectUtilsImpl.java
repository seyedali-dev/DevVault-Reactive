package com.dev.vault.util.project;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.ProjectDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.mappings.ProjectMembers;
import com.dev.vault.model.entity.mappings.UserProjectRole;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.entity.mappings.UserRole;
import com.dev.vault.repository.project.JoinCouponReactiveRepository;
import com.dev.vault.repository.project.JoinProjectRequestReactiveRepository;
import com.dev.vault.repository.mappings.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service implementation for ProjectUtils.
 * Different implementation of `isMember(..)` for JoinRequestService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JoinRequestProjectUtilsImpl implements ProjectUtils {

    private final JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository;
    private final JoinCouponReactiveRepository joinCouponReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectUtils projectUtils;


    /**
     * Checks if the user is already a member of the specified project or has already sent a join project request for the project.
     *
     * @param project the project to check for membership
     * @param user    the user to check for membership
     * @return <code>Mono of true</code> if the user is already a member of the project or has already sent a join project request for the project, false otherwise
     * @throws ResourceNotFoundException      if the project cannot be found
     * @throws ResourceAlreadyExistsException if the user has already sent a join project request for the project
     */
    @Override
    public Mono<Boolean> isMemberOfProject(Project project, User user) {
        return projectMembersReactiveRepository.findByProjectIdAndUserId(project.getProjectId(), user.getUserId())
                .hasElement()
                .map(foundMembers -> foundMembers);
    }

    /**
     * Checks if the JoinRequestCoupon is valid for the specified project.
     *
     * @param project the project to check the JoinRequestCoupon for
     * @return <code>Mono.just(true)</code> if the JoinRequestCoupon is valid, false otherwise
     * @throws DevVaultException         if the JoinRequestCoupon has been used or has exceeded its maximum usage count
     * @throws ResourceNotFoundException if the JoinRequestCoupon cannot be found
     */
    public Mono<Boolean> isCouponValid(Project project) {
        // Retrieve the project from the repository
        return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(project.getProjectId())
                .flatMap(foundProject -> {

                    // Check if the JoinRequestCoupon exists and if it is for the specific project and is for the requesting user (current user is requesting)
                    return authenticationService.getCurrentUserMono()
                            .flatMap(currentUser -> joinCouponReactiveRepository.findByRequestingUserIdAndProjectId(currentUser.getUserId(), project.getProjectId())
                                    .flatMap(joinRequestCoupon -> {

                                        // Check if the JoinRequestCoupon has been used
                                        if (joinRequestCoupon.isUsed()) {
                                            log.error("You have already used this coupon. Please request for another one.");
                                            return Mono.error(new DevVaultException("You have already used this coupon. Please request for another one."));
                                        }

                                        return Mono.just(true);

                                        // if the coupon was not found
                                    }).switchIfEmpty(Mono.defer(() -> {
                                        log.error("Your coupon is invalid");
                                        return Mono.error(new DevVaultException("Your coupon is invalid!"));
                                    }))
                            );
                });
    }


    /**
     * Performs actions when a join request is approved.
     *
     * @param request The join request that was approved.
     */
    @Override
    public Mono<Void> performJoinRequestApprovedActions(JoinProjectRequest request) {
        // Add the user to the project members
        ProjectMembers projectMembers = new ProjectMembers(request.getUserId(), request.getProjectId());
        return projectMembersReactiveRepository.save(projectMembers)
                .then(reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectMembers.getProjectId())
                        .flatMap(project -> {

                            // Increment the member count of the project
                            project.incrementMemberCount();
                            return projectReactiveRepository.save(project)

                                    // Delete the join request
                                    .then(joinProjectRequestReactiveRepository.delete(request));
                        })
                );
    }


    /**
     * Performs actions when a join request is rejected.
     *
     * @param request The join request that was rejected.
     */
    @Override
    public Mono<Void> performJoinRequestRejectedActions(JoinProjectRequest request) {
        // Delete the join request
        return joinProjectRequestReactiveRepository.delete(request);
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

    @Override
    public Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user) {
        return null;
    }

}
