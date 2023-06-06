package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.ResourceAlreadyExistsException;
import com.dev.vault.helper.payload.group.ProjectDto;
import com.dev.vault.model.group.Project;
import com.dev.vault.model.group.ProjectMembers;
import com.dev.vault.model.user.User;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.service.AuthenticationService;
import com.dev.vault.service.ProjectManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {
    private final ProjectMembersRepository projectMembersRepository;
    private final ProjectRepository projectRepository;
    private final ModelMapper modelMapper;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public ProjectDto createProjectOrGroup(ProjectDto projectDto) {
        Optional<Project> foundProject = projectRepository.findByProjectNameAllIgnoreCase(projectDto.getProjectName());
        if (foundProject.isPresent()) {
            log.info("⚠️this project/group already exists! provide a unique name");
            throw new ResourceAlreadyExistsException("Project/Group", "Project/Group Name", foundProject.get().getProjectName());
        }
        User currentUser = authenticationService.getCurrentUser();

        Project project = modelMapper.map(projectDto, Project.class);
        project.setLeader(currentUser);
        projectRepository.save(project);

        ProjectMembers projectMembers = new ProjectMembers(currentUser, project, currentUser.getRoles());
        projectMembersRepository.save(projectMembers);
        
        return ProjectDto.builder()
                .projectName(project.getProjectName())
                .projectDescription(project.getDescription())
                .createdAt(project.getCreatedAt())
                .creationTime(project.getCreationTime())
                .build();
    }
}
