package com.dev.vault.controller.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team_member")
@RequiredArgsConstructor
public class TeamMemberController {
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER') and hasAuthority('team_member:read_only')")
    @GetMapping
    public String get() {
        return "TeamMember :: GET";
    }
}
