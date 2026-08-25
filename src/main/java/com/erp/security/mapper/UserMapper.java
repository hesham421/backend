package com.erp.security.mapper;

import com.erp.security.entity.Permission;
import com.erp.security.entity.Role;
import com.erp.security.entity.UserAccount;
import com.erp.security.dto.UserDto;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    private UserMapper(){}

    public static UserDto toDto(UserAccount u){
        Set<String> roles = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> perms = u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        return new UserDto(
                u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(), roles, perms,
                u.getCreatedAt(), u.getCreatedBy(), u.getUpdatedAt(), u.getUpdatedBy());
    }
}
