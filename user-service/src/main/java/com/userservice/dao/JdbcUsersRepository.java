package com.userservice.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.userservice.model.Role;
import com.userservice.model.User;

@Repository
public class JdbcUsersRepository implements UsersRepository {

    private static final String USER_COLUMNS =
            "id, firstname, lastname, email, phone_number, password_hash, created_at, updated_at, deleted_at";

    private final JdbcTemplate jdbc;
    private final UserRowMapper userMapper;
    private final RoleRowMapper roleMapper;

    public JdbcUsersRepository(JdbcTemplate jdbc, UserRowMapper userMapper, RoleRowMapper roleMapper) {
        this.jdbc = jdbc;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> users = jdbc.query(
                "SELECT " + USER_COLUMNS + " FROM users WHERE email = ? AND deleted_at IS NULL",
                userMapper,
                email);
        return mapWithRoles(users);
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> users = jdbc.query(
                "SELECT " + USER_COLUMNS + " FROM users WHERE id = ? AND deleted_at IS NULL",
                userMapper,
                id);
        return mapWithRoles(users);
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ? AND deleted_at IS NULL",
                Integer.class,
                email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE phone_number = ? AND deleted_at IS NULL",
                Integer.class,
                phoneNumber);
        return count != null && count > 0;
    }

    @Override
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    """
                    INSERT INTO users (firstname, lastname, email, phone_number, password_hash, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFirstname());
            ps.setString(2, user.getLastname());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPhoneNumber());
            ps.setString(5, user.getPasswordHash());
            ps.setTimestamp(6, Timestamp.valueOf(now));
            ps.setTimestamp(7, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to obtain generated user id");
        }

        user.setId(key.longValue());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    @Override
    public void saveUserRole(Long userId, Long roleId) {
        jdbc.update(
                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                userId,
                roleId);
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return jdbc.query(
                """
                SELECT r.name
                FROM roles r
                JOIN user_roles ur ON ur.role_id = r.id
                WHERE ur.user_id = ?
                ORDER BY r.name
                """,
                (rs, rowNum) -> rs.getString("name"),
                userId);
    }

    @Override
    public Optional<Role> findRoleByName(String name) {
        List<Role> roles = jdbc.query(
                "SELECT id, name FROM roles WHERE name = ?",
                roleMapper,
                name);
        return roles.stream().findFirst();
    }

    private Optional<User> mapWithRoles(List<User> users) {
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.getFirst();
        user.setRoles(findRolesByUserId(user.getId()));
        return Optional.of(user);
    }
}
