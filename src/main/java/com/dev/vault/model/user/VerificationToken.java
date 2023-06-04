package com.dev.vault.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    private String token;
    @CreationTimestamp
    private Instant createdAt;
    // TODO: create a expiry date functionality like for 3 hours

    /*relationship*/
    @OneToOne
    private User user;
    /*end of relationship*/

    public VerificationToken(User user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
    }
}
