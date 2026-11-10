package com.userservice.dao;

import java.util.List;
import java.util.Optional;

import com.userservice.model.Role;
import com.userservice.model.User;

public interface UsersRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    User save(User user);

    void saveUserRole(Long userId, Long roleId);

    List<String> findRolesByUserId(Long userId);

    Optional<Role> findRoleByName(String name);
}
