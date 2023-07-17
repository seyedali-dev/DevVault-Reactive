package com.dev.vault.util.project;

import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;

public interface ProjectUtils {
    boolean isLeaderOrAdminOfProject(Project project, User user);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isMemberOfProject(Project project, User user);
}
