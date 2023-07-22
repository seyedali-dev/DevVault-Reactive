package com.dev.vault.util.project;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import reactor.core.publisher.Mono;

public interface ProjectUtils {
    Mono<Boolean> isLeaderOrAdminOfProject(Project project, User user);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    Mono<Boolean> isMemberOfProject(Project project, User user);
}
