package com.dev.vault.controller.project;

import com.dev.vault.helper.payload.response.project.JoinResponse;
import com.dev.vault.service.interfaces.project.JoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.dev.vault.model.enums.JoinStatus.*;


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
     * @param projectId  the ID of the project to send the join project request to
     * @param joinCoupon the coupon that project leader or project admin gave to user
     * @return ResponseEntity containing the JoinResponse object returned by the service
     */
    @PostMapping
    public Mono<ResponseEntity<JoinResponse>> sendJoinRequest(
            @RequestParam String projectId,
            @RequestParam String joinCoupon
    ) {
        return joinRequestService.sendJoinRequest(projectId, joinCoupon)
                .map(ResponseEntity::ok);
    }


    /**
     * Retrieves all join project requests for the specified project with the specified status.
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
