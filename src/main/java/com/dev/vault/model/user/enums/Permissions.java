package com.dev.vault.model.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permissions {
    /*----------------project-leader----------------*/
    PROJECT_LEADER_ACCEPT_JOIN_REQUEST("project_leader:accept_join_request"),
    PROJECT_LEADER_DELETE_MEMBER("project_leader:delete_member"),
    PROJECT_LEADER_READ("project_leader:read"),
    PROJECT_LEADER_CREATE_PROJECT("project_leader:create_project"),
    PROJECT_LEADER_JOIN_REQUEST("project_leader:join_request"),

    /*----------------group-admin----------------*/
    GROUP_ADMIN_ACCEPT_JOIN_REQUEST("group_admin:accept_join_request"),
    GROUP_ADMIN_READ("group_admin:read"),
    GROUP_ADMIN_CREATE_PROJECT("group_admin:create_project"),
    GROUP_ADMIN_JOIN_REQUEST("group_admin:join_request"),


    /*----------------group-admin----------------*/
    TEAM_MEMBER_READ("team_member:read"),
    TEAM_MEMBER_CREATE_PROJECT("team_member:create_project"),
    TEAM_MEMBER_JOIN_REQUEST("team_member:join_request"),

    ;
    @Getter
    private final String permission;
}
