package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.group.SearchResponse;
import com.dev.vault.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for searching projects.
 */
@RestController
@RequestMapping("/api/v1/search_project")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'PROJECT_ADMIN')")
public class SearchController {
    private final SearchService searchService;

    /**
     * Returns a list of all projects.
     *
     * @return a ResponseEntity containing a list of SearchResponse objects
     */
    @GetMapping
    public ResponseEntity<List<SearchResponse>> searchResultForAllProjects() {
        return ResponseEntity.ok(searchService.listAllProjects());
    }

    /**
     * Returns a list of projects that match the provided projectOrGroupName.
     *
     * @param projectOrGroupName the name of the project or group to search for
     * @return a ResponseEntity containing a list of SearchResponse objects
     */
    @GetMapping("/{projectOrGroupName}")
    public ResponseEntity<List<SearchResponse>> searchForAProjectOrGroup(@PathVariable String projectOrGroupName) {
        return ResponseEntity.ok(searchService.searchForProject(projectOrGroupName));
    }
}
