package com.dev.vault.service.interfaces.project;

import com.dev.vault.helper.payload.request.project.JoinProjectDto;
import com.dev.vault.helper.payload.response.project.JoinResponse;
import com.dev.vault.model.enums.JoinStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JoinRequestService {
    Mono<JoinResponse> sendJoinRequest(String projectId, String joinToken);

    Flux<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(String projectId, JoinStatus joinStatus);

    Mono<JoinResponse> updateJoinRequestStatus(String projectId, JoinStatus joinStatus);
}
