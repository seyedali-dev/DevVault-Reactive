package com.dev.vault.repository.group;

import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.enums.JoinStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.List;
import java.util.Optional;

public interface JoinProjectRequestRepository extends ReactiveMongoRepository<JoinProjectRequest, Long> {
    Optional<JoinProjectRequest> findByProject_ProjectIdAndUser_Email(Long projectId, String email);

    List<JoinProjectRequest> findByProject_ProjectIdAndStatus(Long projectId, JoinStatus status);
}