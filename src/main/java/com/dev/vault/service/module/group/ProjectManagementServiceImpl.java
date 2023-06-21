package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.group.ProjectDto;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.group.UserProjectRole;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.group.UserProjectRoleRepository;
import com.dev.vault.repository.user.RolesRepository;
import com.dev.vault.repository.user.UserRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.ProjectManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
 * Service Implementation of Project Creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final UserProjectRoleRepository userProjectRoleRepository;
    private final ProjectMembersRepository projectMembersRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationService authenticationService;

    /**
     * Creates a new project with the provided projectDto.
     *
     * @param projectDto the projectDto containing the project information like name and description
     * @return a ProjectDto object with the project information
     * @throws ResourceAlreadyExistsException if a project with the same name already exists
     * @throws ResourceNotFoundException      if the PROJECT_LEADER role is not found
     */
    @Override
    @Transactional
    public ProjectDto createProject(ProjectDto projectDto) {
        // Check if a project with the same name already exists
        Optional<Project> foundProject = projectRepository.findByProjectNameAllIgnoreCase(projectDto.getProjectName());
        if (foundProject.isPresent()) {
            log.info("⚠️this project already exists! provide a unique name");
            throw new ResourceAlreadyExistsException("Project", "Project Name", foundProject.get().getProjectName());
        }

        // Get the PROJECT_LEADER role
        Roles projectLeaderRole = rolesRepository.findByRole(Role.PROJECT_LEADER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "RoleName", Role.PROJECT_LEADER.name()));

        // Get the current user
        User currentUser = authenticationService.getCurrentUser();

        // Add the PROJECT_LEADER role to the current user and save to db
        currentUser.getRoles().add(projectLeaderRole);
        userRepository.save(currentUser);

        // Map the projectDto to a Project object and set the leader to the current user
        Project project = modelMapper.map(projectDto, Project.class);
        project.setLeader(currentUser);
        project.incrementMemberCount();

        // Save the project to the database
        projectRepository.save(project);

        // Create a new ProjectMembers object for the current user and save it to the database
        ProjectMembers projectMembers = new ProjectMembers(currentUser, project);
        projectMembersRepository.save(projectMembers);

        // Create a new UserProjectRole object for the current user and save it to the database
        UserProjectRole userProjectRole = new UserProjectRole(currentUser, projectLeaderRole, project);
        userProjectRoleRepository.save(userProjectRole);

        // Return a ProjectDto object with the project information
        return ProjectDto.builder()
                .projectName(project.getProjectName())
                .projectDescription(project.getDescription())
                .createdAt(project.getCreatedAt())
                .creationTime(project.getCreationTime())
                .build();
    }
}
