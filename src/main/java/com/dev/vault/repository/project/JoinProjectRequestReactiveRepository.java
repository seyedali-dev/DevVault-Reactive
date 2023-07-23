package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.entity.project.Project;
import com.dev.vault.model.entity.user.User;
import com.dev.vault.model.enums.JoinStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface JoinProjectRequestReactiveRepository extends ReactiveMongoRepository<JoinProjectRequest, Long> {
    Flux<JoinProjectRequest> findByProjectIdAndStatus(String projectId, JoinStatus status);
//    Optional<JoinProjectRequest> findByProject_ProjectIdAndUser_Email(Long projectId, String email);
//
//    List<JoinProjectRequest> findByProject_ProjectIdAndStatus(Long projectId, JoinStatus status);

    Mono<Boolean> existsByProjectIdAndUserId(String project, String user);

}