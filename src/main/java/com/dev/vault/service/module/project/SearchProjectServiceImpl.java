/*
package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.project.ProjectMembers;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.service.interfaces.project.SearchProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Service implementation for searching projects.
 *//*

@Service
@RequiredArgsConstructor
@Slf4j
// TODO:: pagination
public class SearchProjectServiceImpl implements SearchProjectService {

    private final ProjectReactiveRepository projectRepository;
    private final ProjectMembersReactiveRepository projectMembersRepository;

    */
/**
     * Returns a list of all projects with their details.
     *
     * @return A list of SearchResponse objects containing project details.
     *//*

    @Override
    public List<SearchResponse> listAllProjects() {
        // Get all projects from the project repository
        List<Project> projects = projectRepository.findAll();
        // Map each project to a SearchResponse object and collect them into a list
        return projects.stream().map(project ->
                SearchResponse.builder()
                        .projectId(project.getProjectId())
                        .projectName(project.getProjectName())
                        .projectDescription(project.getDescription())
                        .leaderName(project.getLeader().getUsername())
                        .members(new ProjectMembersDto(getUserDtoList(project)))
                        .build()
        ).toList();
    }

    */
/**
     * Searches for a project based on their name and returns their details.
     *
     * @param projectName The name of the project to search for.
     * @return A list of SearchResponse objects containing project details.
     * @throws ResourceNotFoundException If no project is found with the given name.
     *//*

    @Override
    public List<SearchResponse> searchForProject(String projectName) {
        // Search for projects with names containing the given string
        List<Project> project = projectRepository.findByProjectNameContaining(projectName);

        // Throw an exception if no project is found
        if (project == null || project.isEmpty())
            throw new ResourceNotFoundException("Project", "ProjectName", projectName);

        // Map each project to a SearchResponse object and collect them into a list
        return project.stream()
                .map(projects -> SearchResponse.builder()
                        .projectId(projects.getProjectId())
                        .projectName(projects.getProjectName())
                        .projectDescription(projects.getDescription())
                        .leaderName(projects.getLeader().getUsername())
                        .members(new ProjectMembersDto(getUserDtoList(projects)))
                        .build()
                )
                .toList();
    }

    */
/**
     * Returns a list of UserDto objects for a given project.
     *
     * @param project The project to get the list of members for.
     * @return A list of UserDto objects representing the members of the project.
     *//*

    private List<UserDto> getUserDtoList(Project project) {
        // Get all project members associated with the given project
        List<ProjectMembers> members = projectMembersRepository.findByProject(project);

        // Create a list of UserDto objects for the project members
        ArrayList<UserDto> userDtos = new ArrayList<>();
        for (ProjectMembers projectMembers : members) {
            User user = projectMembers.getUser();
            UserDto userDto = UserDto.builder()
                    .username(user.getUsername())
                    .major(user.getMajor())
                    .education(user.getEducation())
                    .role(user.getRoles()
                            .stream().map(roles -> roles.getRole().name()).toList()
                    )
                    .build();
            userDtos.add(userDto);
        }
        return userDtos;
    }
}
*/
