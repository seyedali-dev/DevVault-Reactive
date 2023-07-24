package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.JoinProjectRequest;
import com.dev.vault.model.enums.JoinStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface JoinProjectRequestReactiveRepository extends ReactiveMongoRepository<JoinProjectRequest, String> {

    Flux<JoinProjectRequest> findByProjectIdAndStatus(String projectId, JoinStatus status);

}