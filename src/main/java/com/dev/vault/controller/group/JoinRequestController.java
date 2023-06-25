package com.dev.vault.controller.group;

import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.service.interfaces.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.dev.vault.model.group.enums.JoinStatus.*;

/**
 * REST controller for managing join project requests.
 */
@RestController
@RequestMapping("/api/v1/join_request")
@RequiredArgsConstructor
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    /**
     * Sends a join project request for the specified project.
     *
     * @param projectId the ID of the project to send the join project request to
     * @param joinCoupon the coupon that project leader or project admin gave to user
     * @return ResponseEntity containing the JoinResponse object returned by the service
     */
    @PostMapping({"/{projectId}"})
    public ResponseEntity<JoinResponse> sendJoinRequest(@PathVariable Long projectId, @RequestParam("joinCoupon") String joinCoupon) {
        return ResponseEntity.ok(joinRequestService.sendJoinRequest(projectId, joinCoupon));
    }

    /**
     * Retrieves all join project requests for the specified project (group) with the specified status.
     *
     * @param projectId the ID of the project to retrieve join requests for
     * @return ResponseEntity containing a List of JoinRequest objects with the specified status
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'PROJECT_ADMIN')")
    @GetMapping("/requests/{projectId}")
    public ResponseEntity<?> getAllJoinRequestsByStatus(@PathVariable Long projectId) {
        return ResponseEntity.ok(joinRequestService.getJoinRequestsByProjectIdAndStatus(projectId, PENDING));
    }

    /**
     * Approves the specified join project request.
     *
     * @param joinRequestId the ID of the join project request to approve
     * @return ResponseEntity containing the JoinRequest object after it has been updated
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'PROJECT_ADMIN')")
    @PostMapping("/{joinRequestId}/approve")
    public ResponseEntity<?> approveJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, APPROVED));
    }

    /**
     * Rejects the specified join project request.
     *
     * @param joinRequestId the ID of the join project request to reject
     * @return ResponseEntity containing the JoinRequest object after it has been updated
     */
    @PreAuthorize("hasAnyRole('PROJECT_LEADER', 'PROJECT_ADMIN')")
    @PostMapping("/{joinRequestId}/reject")
    public ResponseEntity<?> rejectJoinRequest(@PathVariable Long joinRequestId) {
        return ResponseEntity.ok(joinRequestService.updateJoinRequestStatus(joinRequestId, REJECTED));
    }
}
