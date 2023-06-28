package com.dev.vault.service.interfaces.project;

public interface JoinCouponService {
    String generateOneTimeJoinCoupon(Long projectId, Long requestingUserId);
}
