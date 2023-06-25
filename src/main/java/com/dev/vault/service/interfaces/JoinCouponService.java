package com.dev.vault.service.interfaces;

public interface JoinCouponService {
    String generateOneTimeJoinCoupon(Long projectId, Long requestingUserId);
}
