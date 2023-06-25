package com.dev.vault.service.interfaces;

import com.dev.vault.helper.payload.group.SearchResponse;

import java.util.List;

public interface SearchService {
    // list all the projects(groups)
    List<SearchResponse> listAllProjects();

    // search for a group with matching names
    List<SearchResponse> searchForProject(String projectOrGroupName);
}
