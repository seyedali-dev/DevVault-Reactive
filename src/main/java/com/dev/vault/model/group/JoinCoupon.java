package com.dev.vault.model.group;

import com.dev.vault.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "join_coupon")
public class JoinCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;
    private String coupon;

    /* relationships */
    @ManyToOne
    @JoinColumn(name = "requesting_user_id")
    private User requestingUser;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    private User leader;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    /* end of relationships */

    private boolean used = false;

    public JoinCoupon(User requestingUser, User leader, Project project) {
        this.coupon = UUID.randomUUID().toString();
        this.requestingUser = requestingUser;
        this.leader = leader;
        this.project = project;
    }
}
