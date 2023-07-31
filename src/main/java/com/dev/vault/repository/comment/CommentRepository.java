package com.dev.vault.repository.comment;

import com.dev.vault.model.domain.comment.Comment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CommentRepository extends ReactiveMongoRepository<Comment, Long> {
}