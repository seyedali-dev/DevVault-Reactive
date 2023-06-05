package com.dev.vault.service;

import com.dev.vault.helper.payload.dto.SearchResponse;

import java.util.List;

public interface SearchProjectService {
    // list all the projects(groups)
    List<SearchResponse> listAllProjects();

    List<SearchResponse> searchForProjectOrGroup(String projectOrGroupName);
}
