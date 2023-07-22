package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.response.project.SearchResponse;
import reactor.core.publisher.Flux;

public interface SearchProjectService {
    // list all the projects
    Flux<SearchResponse> listAllProjects();

    // search for a project with matching names
    Flux<SearchResponse> searchForProject(String projectOrGroupName);
}
