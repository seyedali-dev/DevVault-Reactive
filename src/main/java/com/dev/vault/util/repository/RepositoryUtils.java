package com.dev.vault.util.repository;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.project.Project;
import com.dev.vault.model.task.Task;
import com.dev.vault.model.user.Roles;
import com.dev.vault.model.user.User;
import com.dev.vault.model.user.enums.Role;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.RolesRepository;
import com.dev.vault.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryUtils {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final ProjectRepository projectRepository;

    public User findUserByEmail_OrElseThrow_ResourceNotFoundException(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));
    }

    public Roles findRoleByRole_OrElseThrow_ResourceNotFoundException(Role role) {
        return rolesRepository.findByRole(role)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "RoleName", role.name()));
    }

    public Project findProjectById_OrElseThrow_ResourceNoFoundException(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectID", projectId.toString()));
    }

    public User findUserById_OrElseThrow_ResourceNoFoundException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "UserID", userId.toString()));
    }

    public Task findTaskById_OrElseThrow_ResourceNotFoundException(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "TaskID", taskId.toString()));
    }
}
