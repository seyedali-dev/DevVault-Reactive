package com.dev.vault.controller.authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DemoController {

    @PreAuthorize("hasRole('PROJECT_LEADER')")
    @GetMapping("/pl")
    public Mono<ResponseEntity<String>> pl() {
        return Mono.just(ResponseEntity.ok("""
                Congratulations on successfully implementing JWT with role base.
                This is PROJECT_LEADER
                """)
        );
    }

    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    @GetMapping("/pa")
    public Mono<ResponseEntity<String>> pa() {
        return Mono.just(ResponseEntity.ok("""
                Congratulations on successfully implementing JWT with role base.
                This is PROJECT_ADMIN
                """)
        );
    }

    @PreAuthorize("hasRole('TEAM_MEMBER')")
    @GetMapping("/tm")
    public Mono<ResponseEntity<String>> tm() {
        return Mono.just(ResponseEntity.ok("""
                Congratulations on successfully implementing JWT with role base.
                This is TEAM_MEMBER
                """)
        );
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<String>> all() {
        return Mono.just(ResponseEntity.ok("""
                Congratulations on successfully implementing JWT with role base.
                This is TEAM_MEMBER
                """)
        );
    }

}
