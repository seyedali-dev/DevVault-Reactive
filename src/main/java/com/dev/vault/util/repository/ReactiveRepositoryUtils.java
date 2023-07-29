package com.dev.vault.util.repository;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.entity.mappings.TaskUser;
import com.dev.vault.model.entity.mappings.UserRole;
import com.dev.vault.model.entity.project.JoinCoupon;
import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.task.Task;
import com.dev.vault.model.entity.user.Roles;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.enums.Role;
import com.dev.vault.repository.mappings.TaskUserReactiveRepository;
import com.dev.vault.repository.mappings.UserRoleReactiveRepository;
import com.dev.vault.repository.project.JoinCouponReactiveRepository;
import com.dev.vault.repository.project.JoinProjectRequestReactiveRepository;
import com.dev.vault.repository.project.ProjectReactiveRepository;
import com.dev.vault.repository.task.TaskReactiveRepository;
import com.dev.vault.repository.user.RolesReactiveRepository;
import com.dev.vault.repository.user.UserReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveRepositoryUtils {
    private final TaskUserReactiveRepository taskUserReactiveRepository;

    private final JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository;
    private final UserRoleReactiveRepository userRoleReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final UserReactiveRepository userReactiveRepository;
    private final RolesReactiveRepository rolesReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final JoinCouponReactiveRepository joinCouponReactiveRepository;

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

    public Flux<Roles> findAllRoleByRoleId_OrElseThrow_ResourceNotFoundException(String roleId) {
        return rolesReactiveRepository.findAllByRoleId(roleId)
                .doOnNext(roles -> log.info("Roles found: {}", roles.getRole()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("All Roles", "RoleID", roleId)))
                .doOnError(error -> log.error("Error occurred while finding all the roles by roleID: {}", error.getMessage()));
    }

    public Flux<UserRole> findAllUserRolesByUserId_OrElseThrow_ResourceNotFoundException(String userId) {
        return userRoleReactiveRepository.findAllByUser_UserId(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("UserRoles", "userID", userId)))
                .doOnError(error -> log.error("Error occurred while finding all the user_roles by userID: {}", error.getMessage()));
    }

    public Mono<Project> findProjectById_OrElseThrow_ResourceNotFoundException(String projectId) {
        return projectReactiveRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Project", "ProjectID", projectId)))
                .doOnError(error -> log.error("Error occurred while finding the project by projectID: {}", error.getMessage()));
    }

    public Mono<User> findUserById_OrElseThrow_ResourceNotFoundException(String userId) {
        return userReactiveRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "UserID", userId)))
                .doOnError(error -> log.error("Error occurred while finding the user by userID: {}", error.getMessage()));
    }

    public Mono<Task> findTaskById_OrElseThrow_ResourceNotFoundException(String taskId) {
        return taskReactiveRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Task", "TaskID", taskId)))
                .doOnError(error -> log.error("Error occurred while finding task by taskID: {}", error.getMessage()));
    }

    public Mono<JoinCoupon> findCouponByCoupon_OrElseThrow_ResourceNoFoundException(String joinCoupon) {
        return joinCouponReactiveRepository.findByCoupon(joinCoupon)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("JoinRequestCoupon", "Coupon", joinCoupon)))
                .doOnError(error -> log.error("Error occurred while finding joinCoupon by coupon: {}", error.getMessage()));

    }

    public Mono<JoinProjectRequest> findJoinProjectRequestById_OrElseThrow_ResourceNotFoundException(String joinRequestId) {
        return joinProjectRequestReactiveRepository.findById(joinRequestId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("JoinProjectRequest", "joinRequestId", joinRequestId)))
                .doOnError(error -> log.error("Error occurred while finding joinProjectRequest by joinRequestId: {}", error.getMessage()));
    }

    public Flux<TaskUser> findTaskUsersByUserId_OrElseThrow_ResourceNoFoundException(String assignedToUserId) {
        return taskUserReactiveRepository.findByUser_UserId(assignedToUserId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TaskUser", "assignedToUserId", assignedToUserId)))
                .doOnError(error -> log.error("Error occurred while finding taskUser by assignedToUserId: {}", error.getMessage()));
    }

    public Flux<TaskUser> findTaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(String taskId) {
        return taskUserReactiveRepository.findByTask_TaskId(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TaskUser", "taskId", taskId)))
                .doOnError(error -> log.error("Error occurred while finding taskUser by taskId: {}", error.getMessage()));
    }
}
