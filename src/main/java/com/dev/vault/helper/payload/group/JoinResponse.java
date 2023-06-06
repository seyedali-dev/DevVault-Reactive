package com.dev.vault.helper.payload.group;

import com.dev.vault.model.group.enums.JoinStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class JoinResponse {
    private String status;
    private JoinStatus joinStatus;
}
