package com.dev.vault.model.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permissions {
    /*----------------project-leader----------------*/
    PROJECT_LEADER_ACCEPT_JOIN_REQUEST("project_leader:accept_join_request"),
    PROJECT_LEADER_DELETE_MEMBER("project_leader:delete_member"),

    /*----------------group-admin----------------*/
    GROUP_ADMIN_ACCEPT_JOIN_REQUEST("group_admin:accept_join_request"),

    /*----------------group-admin----------------*/
    TEAM_MEMBER_READ("team_member:read_only")
    ;
    @Getter
    private final String permission;
}
