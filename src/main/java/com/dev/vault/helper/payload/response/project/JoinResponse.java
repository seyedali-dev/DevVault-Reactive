package com.dev.vault.helper.payload.response.project;

import com.dev.vault.model.enums.JoinStatus;
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
