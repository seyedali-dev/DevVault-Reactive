package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.service.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.dev.vault.model.group.enums.JoinStatus.*;

@RestController
@RequestMapping("/api/v1/join_request")
@PreAuthorize("hasAnyRole('PROJECT_LEADER', 'TEAM_MEMBER', 'GROUP_ADMIN')")
@RequiredArgsConstructor // TODO: every group's leader, should only access and manage, it's own groups members, not all the members.
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    // send a join request for a project(group)
    @PostMapping({"/{projectId}"})
    public ResponseEntity<JoinResponse> sendJoinRequest(@PathVariable Long projectId) {
        return ResponseEntity.ok(joinRequestService.sendJoinRequest(projectId));
    }

    // get all the join requests by their status (PENDING)
    @PreAuthorize("hasAnyAuthority('project_leader:read','group_admin:read')")
    @GetMapping("/requests/{projectId}")
    public ResponseEntity<?> getAllJoinRequestsByStatus(@PathVariable Long projectId) {
        return ResponseEntity.ok(joinRequestService.getJoinRequestsByProjectIdAndStatus(projectId, PENDING));
    }

    // approve a join request
    @PreAuthorize("hasAnyAuthority('project_leader:accept_join_request','group_admin:accept_join_request')")
    @PostMapping("/{joinRequestId}/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, APPROVED));
    }

    // reject a join request
    @PreAuthorize("hasAnyAuthority('project_leader:accept_join_request','group_admin:accept_join_request')")
    @PostMapping("/{joinRequestId}/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, REJECTED));
    }
}
