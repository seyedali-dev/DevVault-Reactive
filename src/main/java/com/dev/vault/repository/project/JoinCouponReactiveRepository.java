package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.JoinCoupon;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface JoinCouponReactiveRepository extends ReactiveMongoRepository<JoinCoupon, Long> {
    Mono<JoinCoupon> findByRequestingUserIdAndProjectId(String requestingUserId, String projectId);
}