package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.JoinCoupon;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface JoinCouponRepository extends ReactiveMongoRepository<JoinCoupon, Long> {
    Optional<JoinCoupon> findByProjectAndRequestingUserAndCoupon(Project project, User requestingUser, String coupon);

    Optional<JoinCoupon> findByCoupon(@NonNull String coupon);

    Optional<JoinCoupon> findByRequestingUserAndProject(User requestingUser, Project project);
}