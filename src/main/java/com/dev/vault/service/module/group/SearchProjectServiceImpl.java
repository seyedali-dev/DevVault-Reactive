package com.dev.vault.service.module.group;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.dto.ProjectMembersDto;
import com.dev.vault.helper.payload.dto.SearchResponse;
import com.dev.vault.helper.payload.dto.UserDto;
import com.dev.vault.model.group.Project;
import com.dev.vault.repository.group.ProjectMembersRepository;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.service.SearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchProjectServiceImpl implements SearchProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMembersRepository projectMembersRepository;

    // list all the projects(groups)
    @Override
    public List<SearchResponse> listAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(project ->
                SearchResponse.builder()
                        .projectName(project.getProjectName())
                        .projectDescription(project.getDescription())
                        .leaderName(project.getLeader().getUsername())
                        .members(new ProjectMembersDto(getUserDtoList(project)))
                        .build()
        ).collect(Collectors.toList());
    }

    // search for a group or project based on their name
    @Override
    public List<SearchResponse> searchForProjectOrGroup(String projectOrGroupName) {
        List<Project> project = projectRepository.findByProjectNameContaining(projectOrGroupName);

        if (project == null)
            throw new ResourceNotFoundException("Project(group)", "Project(group) Name", projectOrGroupName);

        return project.stream()
                .map(projects -> SearchResponse.builder()
                        .projectName(projects.getProjectName())
                        .projectDescription(projects.getDescription())
                        .leaderName(projects.getLeader().getUsername())
                        .members(new ProjectMembersDto(getUserDtoList(projects)))
                        .build()
                )
                .toList();
    }


    // create a list of UserDto's for sending list of members to the response
    private List<UserDto> getUserDtoList(Project project) {
        return projectMembersRepository.findByProjectAndUser(project)
                .stream().map(projectMembers ->
                        UserDto.builder()
                                .username(projectMembers.getUser().getUsername())
                                .education(projectMembers.getUser().getEducation())
                                .major(projectMembers.getUser().getMajor())
                                .role(projectMembers.getUser().getRoles().name())
                                .build()
                ).toList();
    }
}
