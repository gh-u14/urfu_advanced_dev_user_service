package com.userservice.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.dao.UsersRepository;
import com.userservice.exception.ForbiddenException;
import com.userservice.exception.NotFoundException;
import com.userservice.model.Role;

@Service
public class RoleService {

    private static final String ADMIN_ROLE = "administrator";

    private final UsersRepository usersRepository;

    public RoleService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Transactional
    public void assignRole(Long actorId, List<String> actorRoles, Long userId, String roleName) {
        requireAdmin(actorRoles);
        ensureUserExists(userId);

        Role role = usersRepository.findRoleByName(roleName)
                .orElseThrow(() -> new NotFoundException("role_not_found", "Роль не найдена"));

        usersRepository.saveUserRole(userId, role.getId());
    }

    @Transactional
    public void revokeRole(Long actorId, List<String> actorRoles, Long userId, String roleName) {
        requireAdmin(actorRoles);
        ensureUserExists(userId);

        Role role = usersRepository.findRoleByName(roleName)
                .orElseThrow(() -> new NotFoundException("role_not_found", "Роль не найдена"));

        usersRepository.deleteUserRole(userId, role.getId());
    }

    private void requireAdmin(List<String> roles) {
        if (roles == null || !roles.contains(ADMIN_ROLE)) {
            throw new ForbiddenException("forbidden", "Требуется роль administrator");
        }
    }

    private void ensureUserExists(Long userId) {
        usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Пользователь не найден"));
    }
}
