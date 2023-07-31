package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.JoinProjectDto;
import com.dev.vault.helper.payload.response.project.JoinResponse;
import com.dev.vault.model.domain.project.JoinCoupon;
import com.dev.vault.model.domain.project.JoinProjectRequest;
import com.dev.vault.model.domain.user.User;
import com.dev.vault.model.enums.JoinStatus;
import com.dev.vault.repository.project.JoinCouponReactiveRepository;
import com.dev.vault.repository.project.JoinProjectRequestReactiveRepository;
import com.dev.vault.service.interfaces.project.JoinRequestService;
import com.dev.vault.service.interfaces.user.AuthenticationService;
import com.dev.vault.util.project.JoinRequestProjectUtilsImpl;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.project.ProjectUtilsImpl;
import com.dev.vault.util.repository.ReactiveRepositoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.dev.vault.model.enums.JoinStatus.PENDING;

/**
 * Service implementation of sending and managing Join Project Requests.
 */
@Slf4j
@Service
public class JoinRequestServiceImpl implements JoinRequestService {

    private final JoinCouponReactiveRepository joinCouponReactiveRepository;
    private final JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository;
    private final AuthenticationService authenticationService;
    private final ReactiveRepositoryUtils reactiveRepositoryUtils;
    private final ProjectUtils projectUtils;
    private final JoinRequestProjectUtilsImpl joinRequestProjectUtilsImpl;


    /**
     * AllArgsConstructor with @Qualifier, since there are two beans of the same type (JoinRequestProjectUtilsImpl & ProjectUtilsImpl)
     */
    @Autowired
    public JoinRequestServiceImpl(
            JoinCouponReactiveRepository joinCouponReactiveRepository,
            JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository,
            AuthenticationService authenticationService, ReactiveRepositoryUtils reactiveRepositoryUtils,
            @Qualifier("projectUtilsImpl") ProjectUtilsImpl projectUtils,
            @Qualifier("joinRequestProjectUtilsImpl") JoinRequestProjectUtilsImpl joinRequestProjectUtilsImpl
    ) {
        this.joinCouponReactiveRepository = joinCouponReactiveRepository;
        this.joinProjectRequestReactiveRepository = joinProjectRequestReactiveRepository;
        this.authenticationService = authenticationService;
        this.reactiveRepositoryUtils = reactiveRepositoryUtils;
        this.projectUtils = projectUtils;
        this.joinRequestProjectUtilsImpl = joinRequestProjectUtilsImpl;
    }


    /**
     * Sends a join project request for the specified project on behalf of the current user. All users are allowed.
     * Only allowed if the user has 'JoinToken' that the project leader, or project admin generated.
     *
     * @param projectId  the ID of the project to send the join project request to
     * @param joinCoupon the coupon that the leader or admin gave to user
     * @return JoinResponse indicating whether the join project request was sent successfully and its status
     * @throws ResourceAlreadyExistsException if the user is already a member of the project or has already sent a join project request for the project
     * @throws ResourceNotFoundException      if the project or user cannot be found
     */
    @Override
    @Transactional
    public Mono<JoinResponse> sendJoinRequest(String projectId, String joinCoupon) {
        // Get the email of the current user
        return authenticationService.getCurrentUserMono()
                .flatMap(currentUser -> {
                    String email = currentUser.getEmail();

                    // Retrieve the project and user from the repositories
                    return reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                            .flatMap(project -> reactiveRepositoryUtils.find_UserByEmail_OrElseThrow_ResourceNotFoundException(email)
                                    .flatMap(user -> {

                                        // Check if the user is already a member of the project or has already sent a join project request for the project
                                        return joinRequestProjectUtilsImpl.isMemberOfProject(project, user)
                                                .flatMap(isMember -> {
                                                    if (isMember) {
                                                        log.error("User {{}}, is not a member of project {{}}", user.getUsername(), project.getProjectName());
                                                        return Mono.error(new ResourceAlreadyExistsException("User: {{" + user.getUsername() + "}} is already a member of the project {{" + project.getProjectName() + "}}"));
                                                    } else {

                                                        // Check if the JoinRequestCoupon is valid
                                                        return joinRequestProjectUtilsImpl.isCouponValid(project)
                                                                .flatMap(isCouponValid -> {
                                                                    if (!isCouponValid) {
                                                                        log.error("coupon is not valid");
                                                                        return Mono.error(new DevVaultException("Invalid JoinRequestCoupon"));
                                                                    }

                                                                    // Mark the JoinRequestCoupon as used
                                                                    return reactiveRepositoryUtils.find_CouponByCoupon_OrElseThrow_ResourceNoFoundException(joinCoupon)
                                                                            .flatMap(joinRequestCoupon -> {
                                                                                joinRequestCoupon.setUsed(true);
                                                                                Mono<JoinCoupon> joinCouponMono = joinCouponReactiveRepository.save(joinRequestCoupon);

                                                                                // Create a new join project request and save it to the repository
                                                                                return joinProjectRequestReactiveRepository.save(new JoinProjectRequest(project.getProjectId(), user.getUserId(), PENDING))
                                                                                        .flatMap(joinProjectRequest -> {

                                                                                            // Delete the JoinRequestCoupon if it has been used
                                                                                            Mono<Void> deletedMono = null;
                                                                                            if (joinRequestCoupon.isUsed())
                                                                                                deletedMono = joinCouponReactiveRepository.delete(joinRequestCoupon);

                                                                                            // Return a JoinResponse indicating that the join project request was sent successfully and its status
                                                                                            return Mono.when(joinCouponMono, Mono.just(joinProjectRequest), deletedMono)
                                                                                                    .then(Mono.fromCallable(() -> JoinResponse.builder()
                                                                                                            .status("Join Project Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                                                                                                            .joinStatus(PENDING)
                                                                                                            .build()));
                                                                                        });
                                                                            });
                                                                });
                                                    }
                                                }).switchIfEmpty(Mono.error(new DevVaultException("No members found")));
                                    }));
                });
    }

    /**
     * Retrieves a list of all join project requests for the specified project with the specified status.
     * Only project leader and project admin are allowed.
     *
     * @param projectId  the ID of the project to retrieve join project requests for
     * @param joinStatus the status of the join requests to retrieve
     * @return a List of JoinProjectDto objects containing information about each join request
     */
    @Override
    public Flux<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(String projectId, JoinStatus joinStatus) {
        return authenticationService.getCurrentUserMono()
                .flatMapMany(currentUser -> reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(projectId)
                        .flatMapMany(project -> {

                            // Check if the current user is the project leader or project admin of the project associated with the join request
                            return projectUtils.isLeaderOrAdminOfProject(project, currentUser)
                                    .flatMapMany(isLeaderOrAdmin -> {
                                        if (isLeaderOrAdmin) {

                                            // Retrieve the join request IDs from the repository
                                            return joinProjectRequestReactiveRepository.findByProjectIdAndStatus(projectId, joinStatus)
                                                    .flatMap(joinRequest -> {

                                                        // Retrieve the user associated with the join request
                                                        Mono<User> userMono = reactiveRepositoryUtils.find_UserById_OrElseThrow_ResourceNotFoundException(joinRequest.getUserId());

                                                        // Map the join request and user to a JoinProjectDto object
                                                        return userMono.map(user -> JoinProjectDto.builder()
                                                                .projectName(project.getProjectName())
                                                                .joinRequestId(joinRequest.getJoinRequestId())
                                                                .joinRequestUsersEmail(user.getEmail())
                                                                .joinStatus(joinRequest.getStatus())
                                                                .build());
                                                    });
                                        } else {
                                            // Throw an exception if the user is not the project leader or admin of the project
                                            return Flux.error(new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻"));
                                        }
                                    });
                        }));
    }


    /**
     * Updates the status of a join request with the given ID and join status (APPROVED, REJECTED).
     *
     * @param joinRequestId The ID of the join request to update.
     * @param joinStatus    The new status of the join request.
     * @return A JoinResponse object with the updated join status.
     * @throws ResourceNotFoundException   If the join request with the given ID is not found.
     * @throws NotLeaderOfProjectException If the user is not the leader or admin of the project.
     */
    @Override
    public Mono<JoinResponse> updateJoinRequestStatus(String joinRequestId, JoinStatus joinStatus) {
        // Find the join request with the given ID
        return reactiveRepositoryUtils.find_JoinProjectRequestById_OrElseThrow_ResourceNotFoundException(joinRequestId)
                .flatMap(request ->
                        authenticationService.getCurrentUserMono().flatMap(currentUser ->
                                reactiveRepositoryUtils.find_ProjectById_OrElseThrow_ResourceNotFoundException(request.getProjectId()).flatMap(project -> {

                                    // Check if the user is the leader or admin of the project
                                    return projectUtils.isLeaderOrAdminOfProject(project, currentUser)
                                            .flatMap(isLeaderOrAdmin -> {
                                                if (!isLeaderOrAdmin) {
                                                    log.error("👮🏻 you are not the leader or admin of this project 👮🏻");
                                                    return Mono.error(new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻"));
                                                }

                                                // Update the status of the join request
                                                request.setStatus(joinStatus);
                                                joinProjectRequestReactiveRepository.save(request)
                                                        .doOnNext(req -> log.info("joinProjectRequest saved successfully: {{}}", req.getStatus()));

                                                // Perform actions based on the new join status
                                                switch (joinStatus) {
                                                    case APPROVED -> {
                                                        return joinRequestProjectUtilsImpl.performJoinRequestApprovedActions(request)
                                                                .then(Mono.just(JoinResponse.builder().joinStatus(joinStatus).build()));
                                                    }
                                                    case REJECTED -> {
                                                        return joinRequestProjectUtilsImpl.performJoinRequestRejectedActions(request)
                                                                .then(Mono.just(JoinResponse.builder().joinStatus(joinStatus).build()));
                                                    }
                                                    default -> {
                                                        return Mono.empty();
                                                    }
                                                }
                                            });
                                })
                        )
                );
    }

}

