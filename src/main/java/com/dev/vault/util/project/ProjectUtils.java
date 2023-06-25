package com.dev.vault.util.project;

import com.dev.vault.model.group.Project;
import com.dev.vault.model.user.User;

public interface ProjectUtils {
    boolean isLeaderOrAdminOfProject(Project project, User user);
    boolean isMemberOfProject(Project project, User user);
}
