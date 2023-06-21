package com.dev.vault.service;

import com.dev.vault.helper.payload.group.JoinProjectDto;
import com.dev.vault.helper.payload.group.JoinResponse;
import com.dev.vault.model.group.enums.JoinStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JoinRequestService {
    // send Join Project Request
    JoinResponse sendJoinRequest(Long projectId, String joinToken);

    List<JoinProjectDto> getJoinRequestsByProjectIdAndStatus(Long projectId, JoinStatus joinStatus);

    JoinResponse updateJoinRequestStatus(Long projectId, JoinStatus joinStatus);
}
