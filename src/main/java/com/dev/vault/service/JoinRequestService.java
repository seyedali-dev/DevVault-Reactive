package com.dev.vault.service;

import com.dev.vault.helper.payload.group.JoinResponse;

public interface JoinRequestService {
    // send Join Request
    JoinResponse sendJoinRequest(Long projectId);
}
