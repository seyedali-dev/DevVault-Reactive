package com.dev.vault.service.module.project;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.request.project.ProjectMembersDto;
import com.dev.vault.helper.payload.request.user.UserDto;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
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
    private final ProjectReactiveRepository projectRepository;
    private final ProjectMembersReactiveRepository projectMembersRepository;
    private final ProjectUtilsImpl projectUtils;


    /**
     * Returns a list of all projects with their details.
     *
     * @return A list of SearchResponse objects containing project details.
     */
    /*@Override
    public Flux<SearchResponse> listAllProjects() {
        // find all the projects in db
        return projectRepository.findAll()
                .flatMap(project -> {
                    Mono<Project> projectMono = Mono.just(project);

                    // find the leader of that specific projects
                    Mono<User> userMono = userReactiveRepository.findByEmail(project.getLeaderEmail())
                            .doOnSuccess(user -> log.info("user found: {{}}", user))
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));

                    // create a list of user dtos for finding the project members of the project
                    Mono<List<UserDto>> projectMembersMono = getUserDtoMonoList(project).collectList();

                    // combine all the asynchronous calls and build a `SearchResponse` object for sending a response
                    return Mono.zip(projectMono, userMono, projectMembersMono)
                            .map(tuple ->
                                    SearchResponse.builder()
                                            .projectId(tuple.getT1().getProjectId())
                                            .projectName(tuple.getT1().getProjectName())
                                            .projectDescription(tuple.getT1().getDescription())
                                            .leaderEmail(tuple.getT2().getUsername())
                                            .members(new ProjectMembersDto(tuple.getT3()))
                                            .build()
                            )
//                            .doOnNext(projectUtils.projectSink::tryEmitNext)
                            ;
                });
    }*/
    @Override
    public Flux<SearchResponse> listAllProjects() {
        // Create a new Flux that emits the newly created project
        Flux<SearchResponse> newProjectFlux = projectUtils.projectSink.asFlux()
                .map(searchResponse -> {
                    log.info("New project emitted: {}", searchResponse.getProjectName());
                    return searchResponse;
                });

        // Create a Flux that emits all the projects from the database
        Flux<SearchResponse> dbProjectsFlux = projectRepository.findAll()
                .flatMap(project -> {
                    Mono<Project> projectMono = Mono.just(project);

                    // find the leader of that specific project
                    Mono<User> userMono = userReactiveRepository.findByEmail(project.getLeaderEmail())
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));

                    // create a list of user dtos for finding the project members of the project
                    Mono<List<UserDto>> projectMembersMono = getUserDtoMonoList(project).collectList();

                    // combine all the asynchronous calls and build a `SearchResponse` object for sending a response
                    return Mono.zip(projectMono, userMono, projectMembersMono)
                            .map(tuple ->
                                    SearchResponse.builder()
                                            .projectId(tuple.getT1().getProjectId())
                                            .projectName(tuple.getT1().getProjectName())
                                            .projectDescription(tuple.getT1().getDescription())
                                            .leaderEmail(tuple.getT2().getUsername())
                                            .members(new ProjectMembersDto(tuple.getT3()))
                                            .build()
                            );
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
                                .leaderEmail(projects.getLeaderEmail())
//                        .members(new ProjectMembersDto(getUserDtoList(projects)))
                                .build()
                ).toList();
    }


    /**
     * Returns a list of UserDto objects for a given project.
     *
     * @param project The project to get the list of members for.
     * @return A list of UserDto objects representing the members of the project.
     */
    public Flux<UserDto> getUserDtoMonoList(Project project) {
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
