package com.dev.vault.helper.payload.group;

import com.dev.vault.model.user.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JoinRequest {
    private User user;
}
