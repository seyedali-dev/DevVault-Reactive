package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.JoinProjectDto;
import com.dev.vault.helper.payload.response.project.JoinResponse;
import com.dev.vault.model.entity.project.JoinCoupon;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.enums.JoinStatus;
import com.dev.vault.repository.project.JoinCouponReactiveRepository;
import com.dev.vault.repository.project.JoinProjectRequestReactiveRepository;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
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
import reactor.core.publisher.Mono;

import java.util.List;

import static com.dev.vault.model.enums.JoinStatus.PENDING;

/**
 * Service implementation of sending and managing Join Project Requests.
 */
@Slf4j
@Service
public class JoinRequestServiceImpl implements JoinRequestService {
    private final JoinCouponReactiveRepository joinCouponReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersRepository;
    private final ProjectReactiveRepository projectRepository;
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
            JoinCouponReactiveRepository joinCouponReactiveRepository, ProjectMembersReactiveRepository projectMembersRepository,
            ProjectReactiveRepository projectRepository, JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository,
            AuthenticationService authenticationService, ReactiveRepositoryUtils reactiveRepositoryUtils,
            @Qualifier("projectUtilsImpl") ProjectUtilsImpl projectUtils,
            @Qualifier("joinRequestProjectUtilsImpl") JoinRequestProjectUtilsImpl joinRequestProjectUtilsImpl
    ) {
        this.joinCouponReactiveRepository = joinCouponReactiveRepository;
        this.projectMembersRepository = projectMembersRepository;
        this.projectRepository = projectRepository;
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
                    return reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId)
                            .flatMap(project -> reactiveRepositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email)
                                    .flatMap(user -> {

                                        // Check if the user is already a member of the project or has already sent a join project request for the project
                                        return joinRequestProjectUtilsImpl.isMemberOfProject(project, user)
                                                .flatMap(isMember -> {
                                                    if (isMember)
                                                        return Mono.error(new ResourceAlreadyExistsException("JoinRequest", "Member", email));
                                                    else {

                                                        // Check if the JoinRequestCoupon is valid
                                                        return joinRequestProjectUtilsImpl.isCouponValid(project)
                                                                .flatMap(isCouponValid -> {
                                                                    if (!isCouponValid)
                                                                        return Mono.error(new DevVaultException("Invalid JoinRequestCoupon"));

                                                                    // Mark the JoinRequestCoupon as used
                                                                    return reactiveRepositoryUtils.findCouponByCoupon_OrElseThrow_ResourceNoFoundException(joinCoupon)
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
                                                });
                                    }));
                });
    }

    //chat, modified my impl
    /*@Override
    @Transactional
    public Mono<JoinResponse> sendJoinRequest(String projectId, String joinCoupon) {
        return authenticationService.getCurrentUserMono()
                .flatMap(user -> reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId)
                        .flatMap(project -> {
                            if (joinProjectUtils.isMemberOfProject(project, user)) {
                                return Mono.error(new ResourceAlreadyExistsException("JoinRequest", "Member", user.getEmail()));
                            } else {
                                return isCouponValid(project)
                                        .flatMap(isValid -> {
                                            if (!isValid) {
                                                return Mono.error(new DevVaultException("Invalid JoinRequestCoupon"));
                                            } else {
                                                return reactiveRepositoryUtils.findCouponByCoupon(joinCoupon)
                                                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("JoinRequestCoupon", "Coupon", joinCoupon)))
                                                        .flatMap(joinRequestCoupon -> {
                                                            joinRequestCoupon.setUsed(true);
                                                            Mono<JoinCoupon> joinCouponMono = joinCouponReactiveRepository.save(joinRequestCoupon);
                                                            JoinProjectRequest joinProjectRequest = new JoinProjectRequest(project.getProjectId(), user.getUserId(), PENDING);
                                                            Mono<JoinProjectRequest> joinProjectRequestMono = joinProjectRequestReactiveRepository.save(joinProjectRequest);
                                                            if (joinRequestCoupon.isUsed()) {
                                                                joinCouponReactiveRepository.delete(joinRequestCoupon).subscribe();
                                                            }
                                                            return Mono.zip(joinCouponMono, joinProjectRequestMono)
                                                                    .map(tuple -> JoinResponse.builder()
                                                                            .status("Join Project Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                                                                            .joinStatus(joinProjectRequest.getStatus().toString())
                                                                            .build());
                                                        });
                                            }
                                        });
                            }
                        }));
    }*/

    //chat new impl
    /*@Override
    @Transactional
    public Mono<JoinResponse> sendJoinRequest(String projectId, String joinCoupon) {
        // Get the email of the current user
        Mono<String> emailMono = authenticationService.getCurrentUserMono().map(User::getEmail);

        // Retrieve the project and user from the repositories
        Mono<Project> projectMono = reactiveRepositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);
        Mono<User> userMono = emailMono.flatMap(reactiveRepositoryUtils::findUserByEmail_OrElseThrow_ResourceNotFoundException);


        // Check if the user is already a member of the project or has already sent a join project request for the project
        Mono<Boolean> isMemberMono = Mono.zip(projectMono, userMono)
                .flatMap(tuple ->
                        joinProjectUtils.isMemberOfProject(tuple.getT1(), tuple.getT2())
                );

        Mono<Boolean> hasJoinRequestSentMono = Mono.zip(projectMono, userMono)
                .flatMap(tuple ->
                        joinProjectRequestReactiveRepository.existsByProjectIdAndUserId(tuple.getT1(), tuple.getT2())
                );

        Mono<Boolean> isCouponValidMono = Mono.zip(projectMono, emailMono)
                .flatMap(tuple ->
                        isCouponValid(tuple.getT1()).onErrorResume(e -> Mono.just(false))
                );

        // Combine the results of all the checks
        return Mono.zip(isMemberMono, hasJoinRequestSentMono, isCouponValidMono)
                .flatMap(tuple -> {
                    if (tuple.getT1() || tuple.getT2())
                        return Mono.error(new ResourceAlreadyExistsException("JoinRequest", "Member", emailMono.block()));

                    if (!tuple.getT3())
                        return Mono.just(new DevVaultException("Invalid JoinRequestCoupon"));

                    // Mark the JoinRequestCoupon as used
                    Mono<JoinCoupon> joinRequestCouponMono = reactiveRepositoryUtils.findCouponByCoupon_OrElseThrow_ResourceNoFoundException(joinCoupon)
                            .doOnNext(joinRequestCoupon -> {
                                joinRequestCoupon.setUsed(true);
                                joinCouponReactiveRepository.save(joinRequestCoupon).subscribe();
                            });

                    // Create a new join project request and save it to the repository
                    Mono<JoinProjectRequest> joinProjectRequestMono = Mono.zip(projectMono, userMono)
                            .map(projectUserTuple ->
                                    new JoinProjectRequest(
                                            projectUserTuple.getT1().getProjectId(),
                                            projectUserTuple.getT2().getUserId(),
                                            PENDING
                                    )
                            ).flatMap(joinProjectRequestReactiveRepository::save);

                    // Delete the JoinRequestCoupon if it has been used
                    Mono<Void> deleteJoinCouponMono = joinRequestCouponMono
                            .filter(JoinCoupon::isUsed)
                            .flatMap(joinCouponReactiveRepository::delete);

                    // Return a JoinResponse indicating that the join project request was sent successfully and its status
                    return Mono.zip(joinProjectRequestMono, deleteJoinCouponMono)
                            .then(Mono.zip(joinRequestCouponMono, joinProjectRequestMono)
                                    .map(joinCouponJoinProjectRequestTuple -> JoinResponse.builder()
                                            .status("Join Project Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                                            .joinStatus(joinCouponJoinProjectRequestTuple.getT2().getStatus())
                                            .build()
                                    )
                            );
                });
    }*/

    @Override
    public List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus) {
        return null;
    }

    @Override
    public JoinResponse updateJoinRequestStatus(Long projectId, JoinStatus joinStatus) {
        return null;
    }

}

