package com.userservice.dao;

import java.util.List;
import java.util.Optional;

import com.userservice.model.Role;
import com.userservice.model.User;

public interface UsersRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAllByIds(List<Long> ids);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailExcludingId(String email, Long excludeId);

    boolean existsByPhoneNumberExcludingId(String phoneNumber, Long excludeId);

    User save(User user);

    User update(User user);

    void softDelete(Long id);

    void saveUserRole(Long userId, Long roleId);

    void deleteUserRole(Long userId, Long roleId);

    boolean hasUserRole(Long userId, Long roleId);

    List<String> findRolesByUserId(Long userId);

    Optional<Role> findRoleByName(String name);
}
