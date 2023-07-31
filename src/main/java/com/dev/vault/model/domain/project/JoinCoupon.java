package com.dev.vault.model.domain.project;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for generating a JoinRequestCoupon for user's that want to make join request to a project.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class JoinCoupon {

    @Id
    private String couponId;
    private String coupon;

    /* relationships */
    private String requestingUserId;
    private String leaderEmail;
    private String projectId;
    /* end of relationships */

    private boolean used = false;

    public JoinCoupon(String requestingUserId, String leaderEmail, String projectId, String coupon) {
        this.requestingUserId = requestingUserId;
        this.leaderEmail = leaderEmail;
        this.projectId = projectId;
        this.coupon = coupon;
    }
}
