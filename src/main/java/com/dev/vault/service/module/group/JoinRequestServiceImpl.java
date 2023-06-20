package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.NotLeaderOfProjectException;
import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.group.JoinProject;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.group.UserProjectRole;
import com.dev.vault.model.group.enums.JoinStatus;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.group.JoinProjectRepository;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.group.UserProjectRoleRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dev.vault.model.group.enums.JoinStatus.*;

/**
 * Service implementation of sending and managing Join Requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
// TODO:: the project leader or project manager will generate a token, and give that token to the user,
//  then he/she can join the project using that token. -> this will secure from unnecessary join requests
public class JoinRequestServiceImpl implements JoinRequestService {
    private final UserProjectRoleRepository userProjectRoleRepository;

    private final ProjectMembersRepository projectMembersRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JoinProjectRepository joinProjectRepository;
    private final AuthenticationService authenticationService;

    /**
     * Sends a join request for the specified project (group) on behalf of the current user.
     * All users are allowed.
     *
     * @param projectId the ID of the project to send the join request to
     * @return JoinResponse indicating whether the join request was sent successfully and its status
     * @throws ResourceAlreadyExistsException if the user is already a member of the project or has already sent a join request for the project
     * @throws ResourceNotFoundException      if the project or user cannot be found
     */
    @Override
    @Transactional
    public JoinResponse sendJoinRequest(Long projectId) {
        String email = authenticationService.getCurrentUser().getEmail();

        // Check if the user is already a member of the project or has already sent a join request for the project
        if (isMember(projectId, email))
            throw new ResourceAlreadyExistsException("JoinRequest", "Member", email);

        // Retrieve the project and user from the repositories
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectId", projectId.toString()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));

        // Create a new join request and save it to the repository
        joinProjectRepository.save(new JoinProject(project, user, PENDING));

        // Return a JoinResponse indicating that the join request was sent successfully and its status
        return JoinResponse.builder()
                .status("Join Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                .joinStatus(PENDING)
                .build();
    }

    /**
     * Checks if the user is already a member of the specified project or has already sent a join request for the project.
     *
     * @param projectId the ID of the project to check
     * @param email     the email address of the user to check
     * @return true if the user is already a member of the project or has already sent a join request for the project, false otherwise
     * @throws ResourceNotFoundException      if the project cannot be found
     * @throws ResourceAlreadyExistsException if the user has already sent a join request for the project
     */
    private boolean isMember(Long projectId, String email) {
        // Retrieve the project from the repository
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectId", projectId.toString()));

        // Check if the user has already sent a join request for the project
        Optional<JoinProject> joinRequest = joinProjectRepository.findByProject_ProjectIdAndUser_Email(projectId, email);
        if (joinRequest.isPresent())
            throw new ResourceAlreadyExistsException("JoinProject", "JoinRequestId", joinRequest.get().getJoinRequestId().toString());

        // Check if the user is already a member of the project
        return projectMembersRepository.findByProject_ProjectNameAndUser_Email(project.getProjectName(), email).isPresent();
    }

    /**
     * Retrieves a list of all join requests for the specified project with the specified status.
     * Only project leader and group admin are allowed.
     *
     * @param projectId  the ID of the project to retrieve join requests for
     * @param joinStatus the status of the join requests to retrieve
     * @return a List of JoinProjectDto objects containing information about each join request
     */
    @Override
    @Transactional
    public List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectId", projectId.toString()));

        // Check if the current user is the project leader or group admin of the project associated with the join request
        if (isLeaderOrAdminOfProject(project)) {
            // Retrieve the join requests from the repository and map them to JoinProjectDto objects
            return joinProjectRepository.findByProject_ProjectIdAndStatus(projectId, joinStatus)
                    .stream().map(joinRequest -> JoinProjectDto.builder()
                            .projectName(joinRequest.getProject().getProjectName())
                            .joinRequestId(joinRequest.getJoinRequestId())
                            .joinRequestUsersEmail(joinRequest.getUser().getEmail())
                            .joinStatus(joinRequest.getStatus())
                            .build()
                    ).collect(Collectors.toList());
        } else {
            // Throw an exception if the user is not the project leader or admin of the group
            throw new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻");
        }
    }

    /**
     * Approves or Rejects a user's project join request and performs the necessary actions based on the new status.
     * Only project leader and group admin are allowed.
     *
     * @param joinRequestId the ID of the join request to update
     * @param joinStatus    the new status of the join request (APPROVED or REJECTED)
     * @return JoinResponse indicating the result of the update operation
     * @throws ResourceNotFoundException   if the join request cannot be found
     * @throws NotLeaderOfProjectException if the user is not the project leader or admin of the group
     */
    @Override
    @Transactional
    public JoinResponse updateJoinRequestStatus(Long joinRequestId, JoinStatus joinStatus) {
        // Retrieve the join request from the repository
        JoinProject request = joinProjectRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("JoinProject", "JoinProjectId", joinRequestId.toString()));

        // Check if the current user is the project leader or group admin of the project associated with the join request
        if (isLeaderOrAdminOfProject(request.getProject())) {
            request.setStatus(joinStatus);
            joinProjectRepository.save(request);

            JoinResponse joinResponse = new JoinResponse();
            // Perform the necessary actions based on the new status
            if (joinStatus.equals(APPROVED)) {
                // If the join request was approved, create a new ProjectMembers object and save it to the repository
                ProjectMembers projectMembers = new ProjectMembers(request.getUser(), request.getProject());
                projectMembersRepository.save(projectMembers);

                joinResponse.setJoinStatus(joinStatus);

                // Delete the join request from the repository
                joinProjectRepository.delete(request);
            } else if (joinStatus.equals(REJECTED)) {
                // If the join request was rejected, delete it from the repository
                joinResponse.setJoinStatus(REJECTED);
                joinProjectRepository.delete(request);
            } else {
                // If the join status is invalid, return null
                return null;
            }
            return joinResponse;
        } else {
            // Throw an exception if the user is not the project leader or admin of the group
            throw new NotLeaderOfProjectException("👮🏻 you are not the leader or admin of this project 👮🏻");
        }
    }

    /**
     * Checks if the current user is the project leader or group admin of the specified project.
     *
     * @param project the project to check
     * @return true if the current user is the project leader or group admin of the project, false otherwise
     */
    private boolean isLeaderOrAdminOfProject(Project project) {

        // Get the current user from the authentication service
        User currentUser = authenticationService.getCurrentUser();

        // Get the roles of the current user that match either PROJECT_LEADER or GROUP_ADMIN
        Roles projectLeaderOrGroupAdminRole =
                currentUser.getRoles().stream()
                        .filter(roles ->
                                roles.getRole().name().equals(Role.PROJECT_LEADER.name())
                                || roles.getRole().name().equals(Role.GROUP_ADMIN.name()))
                        .findFirst()
                        .orElse(null);

        // Get the user's role for the specified project
        Optional<UserProjectRole> userProjectRole =
                userProjectRoleRepository.findByUserAndProjectAndRole(currentUser, project, projectLeaderOrGroupAdminRole);

        // Check if the user has the PROJECT_LEADER or GROUP_ADMIN role for the project
        return userProjectRole.isPresent() &&
               (userProjectRole.get().getRole().getRole() == Role.PROJECT_LEADER ||
                userProjectRole.get().getRole().getRole() == Role.GROUP_ADMIN);
    }
}
