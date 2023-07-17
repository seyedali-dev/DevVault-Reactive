package com.dev.vault.model.entity.project;

import com.dev.vault.model.entity.user.User;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
    @Transient
    private User requestingUser;

    @Transient
    private User leader;

    @Transient
    private Project project;
    /* end of relationships */

    private boolean used = false;

    public JoinCoupon(User requestingUser, User leader, Project project, String coupon) {
        this.requestingUser = requestingUser;
        this.leader = leader;
        this.project = project;
        this.coupon = coupon;
    }
}
