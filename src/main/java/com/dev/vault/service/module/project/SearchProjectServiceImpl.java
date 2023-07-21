package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.repository.project.ProjectMembersReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import com.dev.vault.service.interfaces.project.SearchProjectService;
import com.dev.vault.util.project.ProjectUtilsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * Service implementation for searching projects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
// TODO:: pagination
public class SearchProjectServiceImpl implements SearchProjectService {

    private final UserReactiveRepository userReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final ProjectMembersReactiveRepository projectMembersRepository;
    private final ProjectUtilsImpl projectUtils;


    /**
     * Returns a list of all projects with their details.
     *
     * @return A list of SearchResponse objects containing project details.
     */
    @Override
    public Flux<SearchResponse> listAllProjects() {
        // Create a new Flux that emits the newly created project
        Flux<SearchResponse> newProjectFlux = projectUtils.projectSink.asFlux()
                .map(searchResponse -> {
                    log.info("New project emitted: {}", searchResponse.getProjectName());
                    return searchResponse;
                });

        // Create a Flux that emits all the projects from the database
        Flux<SearchResponse> dbProjectsFlux = projectReactiveRepository.findAll()
                .flatMap(project -> {
                    Mono<Project> projectMono = Mono.just(project);

                    // create a list of user dtos for finding the project members of the project
                    Mono<List<UserDto>> userDtoMonoList = getUserDtoFlux(project).collectList();

                    // combine all the asynchronous calls and build a `SearchResponse` object for sending a response
                    return buildSearchResponseObject(projectMono, userDtoMonoList);
                });

        // Merge the two Fluxes and return the result
        return Flux.merge(newProjectFlux, dbProjectsFlux);
    }


    /**
     * Searches for a project based on their name and returns their details.
     *
     * @param projectName The name of the project to search for.
     * @return A list of SearchResponse objects containing project details.
     * @throws ResourceNotFoundException If no project is found with the given name.
     */
    @Override
    public Flux<SearchResponse> searchForProject(String projectName) {
        // Search for projects with names containing the given string
        return projectReactiveRepository.findByProjectNameContaining(projectName)
                .flatMap(project -> {
                    // Throw an exception if no project is found
                    if (project == null)
                        return Mono.error(new ResourceNotFoundException("Project", "ProjectName", projectName));

                    // Map each project to a SearchResponse object and collect them into a list
                    Mono<List<UserDto>> userDtoMonoList = getUserDtoFlux(project).collectList();
                    Mono<Project> projectMono = Mono.just(project);

                    return buildSearchResponseObject(projectMono, userDtoMonoList);
                });
    }


    private Flux<SearchResponse> buildSearchResponseObject(Mono<Project> projectMono, Mono<List<UserDto>> userDtoMonoList) {
        return Flux.zip(projectMono, userDtoMonoList)
                .map(tuple -> SearchResponse.builder()
                        .projectName(tuple.getT1().getProjectName())
                        .projectDescription(tuple.getT1().getDescription())
                        .projectId(tuple.getT1().getProjectId())
                        .leaderEmail(tuple.getT1().getLeaderEmail())
                        .members(new ProjectMembersDto(tuple.getT2()))
                        .build()
                );
    }


    /**
     * Returns a list of UserDto objects for a given project.
     *
     * @param project The project to get the list of members for.
     * @return A list of UserDto objects representing the members of the project.
     */
    public Flux<UserDto> getUserDtoFlux(Project project) {
        // Get all project members associated with the given project
        return projectMembersRepository.findByProjectId(project.getProjectId())
                .flatMap(members -> {
                    // Create a list of UserDto objects for the project members
                    return userReactiveRepository.findById(members.getUserId())
                            .map(user -> UserDto.builder()
                                    .username(user.getUsername())
                                    .major(user.getMajor())
                                    .education(user.getEducation())
                                    .role(user.getUserRoles().stream()
                                            .map(userRole -> userRole.getRoles().getRole().name())
                                            .toList()
                                    ).build()
                            );
                });
    }

}
