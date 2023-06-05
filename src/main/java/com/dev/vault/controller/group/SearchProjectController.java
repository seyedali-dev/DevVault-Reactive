package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.dto.SearchResponse;
import com.dev.vault.service.SearchProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search_project")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN') " +
              "and hasAnyAuthority('team_member:read','project_leader:read','group_admin:read')")
public class SearchProjectController {
    private final SearchProjectService searchProjectService;

    // finding a specific project
    @GetMapping("/{projectOrGroupName}")
    public ResponseEntity<List<SearchResponse>> searchForAProjectOrGroup(@PathVariable String projectOrGroupName) {
        return ResponseEntity.ok(searchProjectService.searchForProjectOrGroup(projectOrGroupName));
    }

    // finding all projects
    @GetMapping
    public ResponseEntity<List<SearchResponse>> searchResultForAllProjects() {
        return ResponseEntity.ok(searchProjectService.listAllProjects());
    }
}
