package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.group.JoinProject;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.group.enums.JoinStatus;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.JoinProjectRepository;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinRequestServiceImpl implements JoinRequestService {
    private final ProjectMembersRepository projectMembersRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JoinProjectRepository joinProjectRepository;
    private final AuthenticationService authenticationService;

    // send request to join a new project
    @Override
    @Transactional
    public JoinResponse sendJoinRequest(Long projectId) {
        String email = authenticationService.getCurrentUser().getEmail();
        if (isMember(projectId, email))
            throw new ResourceAlreadyExistsException("JoinRequest", "Member", email);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectId", projectId.toString()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));

        joinProjectRepository.save(new JoinProject(project, user, PENDING));

        return JoinResponse.builder()
                .status("Join Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                .joinStatus(PENDING)
                .build();
    }

    // check if the user is already a member of the project or not
    private boolean isMember(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectId", projectId.toString()));

        Optional<JoinProject> joinRequest = joinProjectRepository.findByProject_ProjectIdAndUser_Email(projectId, email);
        if (joinRequest.isPresent())
            throw new ResourceAlreadyExistsException("JoinProject", "JoinRequestId", joinRequest.get().getJoinRequestId().toString());

        return projectMembersRepository.findByProject_ProjectNameAndUser_Email(project.getProjectName(), email).isPresent();
    }

    // get the list of join request by project id, and it's status (PENDING)
    @Override
    @Transactional
    public List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus) {
        return joinProjectRepository.findByProject_ProjectIdAndStatus(projectId, joinStatus)
                .stream().map(joinRequest -> JoinProjectDto.builder()
                        .projectName(joinRequest.getProject().getProjectName())
                        .joinRequestId(joinRequest.getJoinRequestId())
                        .joinRequestUsersEmail(joinRequest.getUser().getEmail())
                        .joinStatus(joinRequest.getStatus())
                        .build()
                ).collect(Collectors.toList());
    }

    // update the status -> (APPROVED or REJECTED)
    @Override
    @Transactional
    public JoinResponse updateJoinRequestStatus(Long joinRequestId, JoinStatus joinStatus) {
        JoinProject request = joinProjectRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("JoinProject", "JoinProjectId", joinRequestId.toString()));
        request.setStatus(joinStatus);
        joinProjectRepository.save(request);

        JoinResponse joinResponse = new JoinResponse();
        if (joinStatus.equals(APPROVED)) {
            ProjectMembers projectMembers = new ProjectMembers(request.getUser(), request.getProject(), request.getUser().getRoles());

            projectMembersRepository.save(projectMembers);
            joinProjectRepository.delete(request);

            joinResponse.setJoinStatus(joinStatus);
        } else if (joinStatus.equals(REJECTED)) {
            joinProjectRepository.delete(request);

            joinResponse.setJoinStatus(REJECTED);
        } else
            return null;

        return joinResponse;
    }
}
