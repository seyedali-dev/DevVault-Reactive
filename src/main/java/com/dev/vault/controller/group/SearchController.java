package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.group.SearchResponse;
import com.dev.vault.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search_project")
@RequiredArgsConstructor
//@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN') " +
//              "and hasAnyAuthority('team_member:read','project_leader:read','group_admin:read')")
@PreAuthorize("hasAnyAuthority('team_member:read','project_leader:read','group_admin:read')")
public class SearchController {
    private final SearchService searchService;

    // finding all projects
    @GetMapping
    public ResponseEntity<List<SearchResponse>> searchResultForAllProjects() {
        return ResponseEntity.ok(searchService.listAllProjects());
    }

    // finding a specific project
    @GetMapping("/{projectOrGroupName}")
    public ResponseEntity<List<SearchResponse>> searchForAProjectOrGroup(@PathVariable String projectOrGroupName) {
        return ResponseEntity.ok(searchService.searchForProjectOrGroup(projectOrGroupName));
    }
}
