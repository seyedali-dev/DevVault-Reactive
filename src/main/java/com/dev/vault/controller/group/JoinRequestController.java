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
@RequiredArgsConstructor
public class JoinRequestController { // TODO: every group's leader, should only access and manage, it's own groups members, not all the members.

    private final JoinRequestService joinRequestService;

    /**
     * Sends a join request for the specified project.
     *
     * @param projectId the ID of the project to send the join request to
     * @return ResponseEntity containing the JoinResponse object returned by the service
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'GROUP_ADMIN','TEAM_MEMBER')")
    @PostMapping({"/{projectId}"})
    public ResponseEntity<JoinResponse> sendJoinRequest(@PathVariable Long projectId) {
        return ResponseEntity.ok(joinRequestService.sendJoinRequest(projectId));
    }

    /**
     * Retrieves all join requests for the specified project (group) with the specified status.
     *
     * @param projectId the ID of the project to retrieve join requests for
     * @return ResponseEntity containing a List of JoinRequest objects with the specified status
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'GROUP_ADMIN')")
    @GetMapping("/requests/{projectId}")
    public ResponseEntity<?> getAllJoinRequestsByStatus(@PathVariable Long projectId) {
        return ResponseEntity.ok(joinRequestService.getJoinRequestsByProjectIdAndStatus(projectId, PENDING));
    }

    /**
     * Approves the specified join request.
     *
     * @param joinRequestId the ID of the join request to approve
     * @return ResponseEntity containing the JoinRequest object after it has been updated
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'GROUP_ADMIN')")
    @PostMapping("/{joinRequestId}/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, APPROVED));
    }

    /**
     * Rejects the specified join request.
     *
     * @param joinRequestId the ID of the join request to reject
     * @return ResponseEntity containing the JoinRequest object after it has been updated
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'GROUP_ADMIN')")
    @PostMapping("/{joinRequestId}/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, REJECTED));
    }
}
