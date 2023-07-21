package com.dev.vault.controller.project;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.helper.payload.response.project.SearchResponse;
import com.dev.vault.service.interfaces.project.SearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

/**
 * REST controller for searching projects.
 */
@RestController
@RequestMapping("/api/v1/search_project")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'PROJECT_ADMIN')")
public class SearchController {

    private final SearchProjectService searchProjectService;


    /**
     * Returns a stream of all projects.
     *
     * @return a stream of SearchResponse objects in Server-Sent Events (SSE) format
     */
    @GetMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
    public Flux<SearchResponse> getProjectDtosStream() {
        return searchProjectService.listAllProjects();
    }


    /**
     * Returns a list of projects that match the provided projectName.
     *
     * @param projectName the name of the project to search for
     * @return a ResponseEntity containing a list of SearchResponse objects
     */
    @GetMapping("/projectName/{projectName}")
    public Mono<ResponseEntity<Flux<SearchResponse>>> searchForProject(@PathVariable String projectName) {
        Mono<List<SearchResponse>> searchResultsMonoList = searchProjectService.searchForProject(projectName).collectList();
        return searchResultsMonoList
                .flatMap(searchResponseList -> {
                            if (searchResponseList.isEmpty())
                                return Mono.error(new ResourceNotFoundException("Project", "ProjectName", projectName));
                            else
                                return Mono.just(ResponseEntity.ok(Flux.fromIterable(searchResponseList)));
                        }
                );
    }

}
