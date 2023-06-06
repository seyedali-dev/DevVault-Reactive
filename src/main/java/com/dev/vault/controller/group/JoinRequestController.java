package com.dev.vault.controller.group;

import com.dev.vault.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/join_request")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN')")
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    // send a join request for a project(group)
    @PostMapping({"/{projectId}"})
    public ResponseEntity<?> sendJoinRequest(@PathVariable Long projectId) {
        return new ResponseEntity<>(joinRequestService.sendJoinRequest(projectId), HttpStatus.PROCESSING);
    }
}
