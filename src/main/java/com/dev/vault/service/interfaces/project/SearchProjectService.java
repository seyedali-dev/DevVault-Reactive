package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.response.project.SearchResponse;

import java.util.List;

public interface SearchProjectService {
    // list all the projects
    List<SearchResponse> listAllProjects();

    // search for a project with matching names
    List<SearchResponse> searchForProject(String projectOrGroupName);
}
