package com.dev.vault.util.repository;

import com.dev.vault.helper.exception.ResourceNotFoundException;
import com.dev.vault.model.domain.project.JoinCoupon;
import com.dev.vault.model.domain.project.JoinProjectRequest;
import com.dev.vault.model.domain.project.Project;
import com.dev.vault.model.domain.relationship.ProjectTask;
import com.dev.vault.model.domain.relationship.TaskUser;
import com.dev.vault.model.domain.relationship.UserRole;
import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.domain.user.Roles;
import com.dev.vault.model.domain.user.User;
import com.dev.vault.model.enums.Role;
import com.dev.vault.repository.mappings.ProjectTaskReactiveRepository;
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

    private final ProjectTaskReactiveRepository projectTaskReactiveRepository;
    private final TaskUserReactiveRepository taskUserReactiveRepository;
    private final JoinProjectRequestReactiveRepository joinProjectRequestReactiveRepository;
    private final UserRoleReactiveRepository userRoleReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final UserReactiveRepository userReactiveRepository;
    private final RolesReactiveRepository rolesReactiveRepository;
    private final ProjectReactiveRepository projectReactiveRepository;
    private final JoinCouponReactiveRepository joinCouponReactiveRepository;

    public Mono<User> find_UserByEmail_OrElseThrow_ResourceNotFoundException(String email) {
        return userReactiveRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "Email", email)))
                .doOnError(error -> log.error("Error occurred while finding user by email: {}", error.getMessage()));
    }

    public Mono<Roles> find_RoleByRole_OrElseThrow_ResourceNotFoundException(Role role) {
        return rolesReactiveRepository.findByRole(role)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Role", "RoleName", role.name())))
                .doOnError(error -> log.error("Error occurred while finding role by role: {}", error.getMessage()));
    }

    public Flux<Roles> find_AllRoleByRoleId_OrElseThrow_ResourceNotFoundException(String roleId) {
        return rolesReactiveRepository.findAllByRoleId(roleId)
                .doOnNext(roles -> log.info("Roles found: {}", roles.getRole()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("All Roles", "RoleID", roleId)))
                .doOnError(error -> log.error("Error occurred while finding all the roles by roleID: {}", error.getMessage()));
    }

    public Flux<UserRole> find_AllUserRolesByUserId_OrElseThrow_ResourceNotFoundException(String userId) {
        return userRoleReactiveRepository.findAllByUser_UserId(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("UserRoles", "userID", userId)))
                .doOnError(error -> log.error("Error occurred while finding all the user_roles by userID: {}", error.getMessage()));
    }

    public Mono<Project> find_ProjectById_OrElseThrow_ResourceNotFoundException(String projectId) {
        return projectReactiveRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Project", "ProjectID", projectId)))
                .doOnError(error -> log.error("Error occurred while finding the project by projectID: {}", error.getMessage()));
    }

    public Mono<User> find_UserById_OrElseThrow_ResourceNotFoundException(String userId) {
        return userReactiveRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", "UserID", userId)))
                .doOnError(error -> log.error("Error occurred while finding the user by userID: {}", error.getMessage()));
    }

    public Mono<Task> find_TaskById_OrElseThrow_ResourceNotFoundException(String taskId) {
        return taskReactiveRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Task", "TaskID", taskId)))
                .doOnError(error -> log.error("Error occurred while finding task by taskID: {}", error.getMessage()));
    }

    public Mono<JoinCoupon> find_CouponByCoupon_OrElseThrow_ResourceNoFoundException(String joinCoupon) {
        return joinCouponReactiveRepository.findByCoupon(joinCoupon)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("JoinRequestCoupon", "Coupon", joinCoupon)))
                .doOnError(error -> log.error("Error occurred while finding joinCoupon by coupon: {}", error.getMessage()));

    }

    public Mono<JoinProjectRequest> find_JoinProjectRequestById_OrElseThrow_ResourceNotFoundException(String joinRequestId) {
        return joinProjectRequestReactiveRepository.findById(joinRequestId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("JoinProjectRequest", "joinRequestId", joinRequestId)))
                .doOnError(error -> log.error("Error occurred while finding joinProjectRequest by joinRequestId: {}", error.getMessage()));
    }

    public Flux<TaskUser> find_TaskUsersByTaskId_OrElseThrow_ResourceNotFoundException(String taskId) {
        return taskUserReactiveRepository.findByTask_TaskId(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TaskUser", "taskId", taskId)))
                .doOnError(error -> log.error("Error occurred while finding taskUser by taskId: {}", error.getMessage()));
    }

    public Mono<TaskUser> find_TaskUserByTaskId_OrElseThrow_ResourceNotFoundException(String taskId) {
        return taskUserReactiveRepository.findTaskUserByTask_TaskId(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TaskUser", "taskId", taskId)))
                .doOnError(error -> log.error("Error occurred while finding taskUser by taskId: {}", error.getMessage()));
    }

    public Mono<ProjectTask> find_ProjectTaskByTaskId_OrElseThrow_ResourceNotFoundException(String taskId) {
        return projectTaskReactiveRepository.findProjectTaskByTask_TaskId(taskId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("TaskUser", "taskId", taskId)))
                .doOnError(error -> log.error("Error occurred while finding taskUser by taskId: {}", error.getMessage()));
    }
}
