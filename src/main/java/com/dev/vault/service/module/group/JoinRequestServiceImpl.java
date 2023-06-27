package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.project.JoinCoupon;
import com.dev.vault.model.project.JoinProjectRequest;
import com.dev.vault.model.project.Project;
import com.dev.vault.model.project.ProjectMembers;
import com.dev.vault.model.project.enums.JoinStatus;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.JoinCouponRepository;
import com.dev.vault.repository.group.JoinProjectRequestRepository;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.service.interfaces.AuthenticationService;
import com.dev.vault.service.interfaces.JoinRequestService;
import com.dev.vault.util.project.JoinRequestProjectUtilsImpl;
import com.dev.vault.util.project.ProjectUtils;
import com.dev.vault.util.project.ProjectUtilsImpl;
import com.dev.vault.util.repository.RepositoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dev.vault.model.project.enums.JoinStatus.PENDING;

/**
 * Service implementation of sending and managing Join Project Requests.
 */
@Service
@Slf4j
public class JoinRequestServiceImpl implements JoinRequestService {
    private final JoinCouponRepository joinCouponRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final ProjectRepository projectRepository;
    private final JoinProjectRequestRepository joinProjectRequestRepository;
    private final AuthenticationService authenticationService;
    private final RepositoryUtils repositoryUtils;
    private final ProjectUtils projectUtils;
    private final JoinRequestProjectUtilsImpl joinProjectUtils;

    /**
     * AllArgsConstructor with @Qualifier, since there are two beans of the same type (JoinRequestProjectUtilsImpl & ProjectUtilsImpl)
     */
    @Autowired
    public JoinRequestServiceImpl(JoinCouponRepository joinCouponRepository, ProjectMembersRepository projectMembersRepository, ProjectRepository projectRepository, JoinProjectRequestRepository joinProjectRequestRepository, AuthenticationService authenticationService, RepositoryUtils repositoryUtils, @Qualifier("projectUtilsImpl") ProjectUtilsImpl projectUtils, @Qualifier("joinRequestProjectUtilsImpl") JoinRequestProjectUtilsImpl joinProjectUtils) {
        this.joinCouponRepository = joinCouponRepository;
        this.projectMembersRepository = projectMembersRepository;
        this.projectRepository = projectRepository;
        this.joinProjectRequestRepository = joinProjectRequestRepository;
        this.authenticationService = authenticationService;
        this.repositoryUtils = repositoryUtils;
        this.projectUtils = projectUtils;
        this.joinProjectUtils = joinProjectUtils;
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
    public JoinResponse sendJoinRequest(Long projectId, String joinCoupon) {
        // Get the email of the current user
        String email = authenticationService.getCurrentUser().getEmail();

        // Retrieve the project and user from the repositories
        Project project = repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);
        User user = repositoryUtils.findUserByEmail_OrElseThrow_ResourceNotFoundException(email);

        // Check if the user is already a member of the project or has already sent a join project request for the project
        if (joinProjectUtils.isMemberOfProject(project, user))
            throw new ResourceAlreadyExistsException("JoinRequest", "Member", email);

        // Check if the JoinRequestCoupon is valid
        if (!isCouponValid(project, joinCoupon))
            throw new DevVaultException("Invalid JoinRequestCoupon");

        // Mark the JoinRequestCoupon as used
        JoinCoupon joinRequestCoupon = joinCouponRepository.findByCoupon(joinCoupon)
                .orElseThrow(() -> new ResourceNotFoundException("JoinRequestCoupon", "Coupon", joinCoupon));
        joinRequestCoupon.setUsed(true);
        joinCouponRepository.save(joinRequestCoupon);

        // Create a new join project request and save it to the repository
        joinProjectRequestRepository.save(new JoinProjectRequest(project, user, PENDING));

        // Delete the JoinRequestCoupon if it has been used
        if (joinRequestCoupon.isUsed())
            joinCouponRepository.delete(joinRequestCoupon);

        // Return a JoinResponse indicating that the join project request was sent successfully and its status
        return JoinResponse.builder()
                .status("Join Project Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                .joinStatus(PENDING)
                .build();
    }

    /**
     * Checks if the JoinRequestCoupon is valid for the specified project.
     *
     * @param project    the project to check the JoinRequestCoupon for
     * @param joinCoupon the JoinRequestCoupon to check
     * @return true if the JoinRequestCoupon is valid, false otherwise
     * @throws DevVaultException         if the JoinRequestCoupon has been used or has exceeded its maximum usage count
     * @throws ResourceNotFoundException if the JoinRequestCoupon cannot be found
     */
    private boolean isCouponValid(Project project, String joinCoupon) {
        // Retrieve the project from the repository
        repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(project.getProjectId());

        // Check if the JoinRequestCoupon exists and if it is for the specific project and is for the requesting user (current user is requesting)
        User currentUser = authenticationService.getCurrentUser();
        Optional<JoinCoupon> joinRequestCoupon = joinCouponRepository
                .findByProjectAndRequestingUserAndCoupon(project, currentUser, joinCoupon);

        if (joinRequestCoupon.isEmpty())
            throw new DevVaultException("This JoinRequestCoupon is either; " +
                                        "1. Not for this project: {" + project.getProjectName() + "}" +
                                        " | 2. Not for this user: {" + currentUser.getUsername() + "}");

        // Check if the JoinRequestCoupon has been used
        if (joinRequestCoupon.get().isUsed())
            throw new DevVaultException("You have already used this coupon. Please request for another one.");

        return true;
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
    @Transactional
    public List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus) {
        Project project = repositoryUtils.findProjectById_OrElseThrow_ResourceNoFoundException(projectId);

        // Check if the current user is the project leader or project admin of the project associated with the join request
        if (projectUtils.isLeaderOrAdminOfProject(project, authenticationService.getCurrentUser())) {
            // Retrieve the join requests from the repository and map them to JoinProjectDto objects
            return joinProjectRequestRepository.findByProject_ProjectIdAndStatus(projectId, joinStatus)
                    .stream().map(joinRequest -> JoinProjectDto.builder()
                            .projectName(joinRequest.getProject().getProjectName())
                            .joinRequestId(joinRequest.getJoinRequestId())
                            .joinRequestUsersEmail(joinRequest.getUser().getEmail())
                            .joinStatus(joinRequest.getStatus())
                            .build()
                    ).collect(Collectors.toList());
        } else {
            // Throw an exception if the user is not the project leader or admin of the project
            throw new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻");
        }
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
    @Transactional
    public JoinResponse updateJoinRequestStatus(Long joinRequestId, JoinStatus joinStatus) {
        // Find the join request with the given ID
        JoinProjectRequest request = joinProjectRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("JoinProjectRequest", "JoinProjectId", joinRequestId.toString()));

        // Check if the user is the leader or admin of the project
        if (!projectUtils.isLeaderOrAdminOfProject(request.getProject(), authenticationService.getCurrentUser()))
            throw new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻");

        // Update the status of the join request
        request.setStatus(joinStatus);
        joinProjectRequestRepository.save(request);

        // Perform actions based on the new join status
        switch (joinStatus) {
            case APPROVED -> {
                performJoinRequestApprovedActions(request);
                return JoinResponse.builder().joinStatus(joinStatus).build();
            }
            case REJECTED -> {
                performJoinRequestRejectedActions(request);
                return JoinResponse.builder().joinStatus(joinStatus).build();
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Performs actions when a join request is approved.
     *
     * @param request The join request that was approved.
     */
    private void performJoinRequestApprovedActions(JoinProjectRequest request) {
        // Add the user to the project members
        ProjectMembers projectMembers = new ProjectMembers(request.getUser(), request.getProject());
        projectMembersRepository.save(projectMembers);

        // Increment the member count of the project
        Project project = projectMembers.getProject();
        project.incrementMemberCount();
        projectRepository.save(project);

        // Delete the join request
        joinProjectRequestRepository.delete(request);
    }

    /**
     * Performs actions when a join request is rejected.
     *
     * @param request The join request that was rejected.
     */
    private void performJoinRequestRejectedActions(JoinProjectRequest request) {
        // Delete the join request
        joinProjectRequestRepository.delete(request);
    }
}
