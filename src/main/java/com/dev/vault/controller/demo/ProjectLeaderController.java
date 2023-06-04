package com.dev.vault.controller.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/proj_leader")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PROJECT_LEADER')")
public class ProjectLeaderController {
    @GetMapping
    public String get() {
        return "ProjectLeader :: GET";
    }
}
