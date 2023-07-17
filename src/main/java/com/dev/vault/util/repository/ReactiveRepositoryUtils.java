package com.dev.vault.util.repository;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.enums.Role;
import com.dev.vault.repository.group.ProjectRepository;
import com.dev.vault.repository.task.TaskRepository;
import com.dev.vault.repository.user.RolesReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveRepositoryUtils {
    private final TaskRepository taskRepository;
    private final UserReactiveRepository userReactiveRepository;
    private final RolesReactiveRepository rolesReactiveRepository;
    private final ProjectRepository projectRepository;

    public Mono<User> findUserByEmail_OrElseThrow_ResourceNotFoundException(String email) {
        return userReactiveRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "Email", email)))
                .doOnError(error -> log.error("Error occurred while finding user by email: {}", error.getMessage()));
    }

    public Mono<Roles> findRoleByRole_OrElseThrow_ResourceNotFoundException(Role role) {
        return rolesReactiveRepository.findByRole(role)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Role", "RoleName", role.name())))
                .doOnError(error -> log.error("Error occurred while finding role by role: {}", error.getMessage()));
    }

  /*  public Project findProjectById_OrElseThrow_ResourceNoFoundException(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "ProjectID", projectId.toString()));
    }

    public User findUserById_OrElseThrow_ResourceNoFoundException(Long userId) {
        return userReactiveRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "UserID", userId.toString()));
    }

    public Task findTaskById_OrElseThrow_ResourceNotFoundException(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "TaskID", taskId.toString()));
    }*/
}
