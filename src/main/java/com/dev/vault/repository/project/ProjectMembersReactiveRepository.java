package com.dev.vault.repository.project;

import com.dev.vault.model.entity.project.ProjectMembers;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProjectMembersReactiveRepository extends ReactiveMongoRepository<ProjectMembers, String> {

}