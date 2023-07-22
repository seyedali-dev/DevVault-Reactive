package com.dev.vault.service.interfaces.project;

import reactor.core.publisher.Mono;

public interface JoinCouponService {
    Mono<String> generateOneTimeJoinCoupon(String projectId, String requestingUserId);
}
