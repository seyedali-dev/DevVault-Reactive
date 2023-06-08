package com.dev.vault.service;

import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.group.enums.JoinStatus;

import java.util.List;

public interface JoinRequestService {
    // send Join Request
    JoinResponse sendJoinRequest(Long projectId);

    List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus);

    JoinResponse updateJoinRequestStatus(Long projectId, JoinStatus joinStatus);
}
