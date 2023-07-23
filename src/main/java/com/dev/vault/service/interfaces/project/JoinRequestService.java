package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.request.project.JoinProjectDto;
import com.dev.vault.helper.payload.response.project.JoinResponse;
import com.dev.vault.model.enums.JoinStatus;
import reactor.core.publisher.Mono;

import java.util.List;

public interface JoinRequestService {
    Mono<JoinResponse> sendJoinRequest(String projectId, String joinToken);

    List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus);

    JoinResponse updateJoinRequestStatus(Long projectId, JoinStatus joinStatus);
}
