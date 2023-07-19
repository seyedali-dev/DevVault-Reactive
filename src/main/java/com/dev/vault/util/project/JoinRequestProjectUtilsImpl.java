/*
package com.dev.vault.util.project;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.ProjectMembers;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.repository.project.JoinProjectRequestRepository;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

*/
/**
 * Service implementation for ProjectUtils.
 * Different implementation of `isMember(..)` for JoinRequestService.
 *//*

@Service
@RequiredArgsConstructor
@Slf4j
public class JoinRequestProjectUtilsImpl implements ProjectUtils {
    private final ProjectMembersReactiveRepository projectMembersRepository;
    private final JoinProjectRequestRepository joinProjectRequestRepository;

    */
/**
     * Default implementation.
     *//*

    @Override
    public boolean isLeaderOrAdminOfProject(Project project, User user) {
        return false;
    }

    */
/**
     * Checks if the user is already a member of the specified project or has already sent a join project request for the project.
     *
     * @param project the project to check for membership
     * @param user    the user to check for membership
     * @return true if the user is already a member of the project or has already sent a join project request for the project, false otherwise
     * @throws ResourceNotFoundException      if the project cannot be found
     * @throws ResourceAlreadyExistsException if the user has already sent a join project request for the project
     *//*

    @Override
    public boolean isMemberOfProject(Project project, User user) {
        // Check if the user has already sent a join project request for the project
        Optional<JoinProjectRequest> joinRequest = joinProjectRequestRepository.findByProject_ProjectIdAndUser_Email(project.getProjectId(), user.getEmail());
        if (joinRequest.isPresent())
            throw new ResourceAlreadyExistsException("JoinProjectRequest", "JoinRequestId", joinRequest.get().getJoinRequestId().toString());

        Optional<ProjectMembers> member = projectMembersRepository.findByProject_ProjectNameAndUser_Email(project.getProjectName(), user.getEmail());
        // Check if the user is already a member of the project
        return member.isPresent();
    }
}
*/
