package com.dev.vault.util.project;

import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProjectUtils {
    Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    Mono<Boolean> isMemberOfProject(Project project, User user);

    Mono<Boolean> isCouponValid(Project project);

    Mono<Void> performJoinRequestApprovedActions(JoinProjectRequest request);

    Mono<Void> performJoinRequestRejectedActions(JoinProjectRequest request);

}
