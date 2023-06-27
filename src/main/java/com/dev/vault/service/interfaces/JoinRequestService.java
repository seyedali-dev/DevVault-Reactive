package com.dev.vault.service.interfaces;

import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.project.enums.JoinStatus;

import java.util.List;

public interface JoinRequestService {
    // send Join Project Request
    JoinResponse sendJoinRequest(Long projectId, String joinToken);

    List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus);

    JoinResponse updateJoinRequestStatus(Long projectId, JoinStatus joinStatus);
}
