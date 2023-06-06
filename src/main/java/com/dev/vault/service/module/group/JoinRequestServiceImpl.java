package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.group.JoinProject;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.enums.JoinStatus;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.JoinProjectRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinRequestServiceImpl implements JoinRequestService {
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

        joinProjectRepository.save(new JoinProject(project, user, JoinStatus.PENDING));

        return JoinResponse.builder()
                .status("Join Request Sent successfully. Please wait until ProjectLeader approves your request :)")
                .joinStatus(JoinStatus.PENDING)
                .build();
    }

    // check if the user is already a member of the project or not
    private boolean isMember(Long projectId, String email) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectName", projectId.toString()));
        return joinProjectRepository.findByProject_ProjectName(project.getProjectName())
                .stream().anyMatch(
                        projectMembers -> projectMembers.getUser().getEmail().equals(email)
                );
    }
}
