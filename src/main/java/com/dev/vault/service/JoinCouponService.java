package com.dev.vault.service;

public interface JoinCouponService {
    String generateOneTimeJoinCoupon(Long projectId, Long requestingUserId);
}
