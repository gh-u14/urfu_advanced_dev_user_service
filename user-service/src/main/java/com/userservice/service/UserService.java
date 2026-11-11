package com.userservice.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.dao.UsersRepository;
import com.userservice.dto.ItemsResponse;
import com.userservice.dto.PublicUserResponse;
import com.userservice.dto.UpdateUserRequest;
import com.userservice.dto.UserResponse;
import com.userservice.exception.BadRequestException;
import com.userservice.exception.ConflictException;
import com.userservice.exception.NotFoundException;
import com.userservice.model.User;
import com.userservice.util.UserMapper;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;

    public UserService(UsersRepository usersRepository, UserMapper userMapper) {
        this.usersRepository = usersRepository;
        this.userMapper = userMapper;
    }

    public UserResponse getMe(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Пользователь не найден"));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Пользователь не найден"));

        if (request.getFirstname() != null) {
            if (request.getFirstname().isBlank()) {
                throw new BadRequestException("invalid_firstname", "firstname не может быть пустым");
            }
            user.setFirstname(request.getFirstname().trim());
        }

        if (request.getLastname() != null) {
            if (request.getLastname().isBlank()) {
                throw new BadRequestException("invalid_lastname", "lastname не может быть пустым");
            }
            user.setLastname(request.getLastname().trim());
        }

        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase();
            if (email.isBlank() || !email.contains("@")) {
                throw new BadRequestException("invalid_email", "Некорректный email");
            }
            if (usersRepository.existsByEmailExcludingId(email, userId)) {
                throw new ConflictException("email_taken", "Email уже занят");
            }
            user.setEmail(email);
        }

        if (request.getPhoneNumber() != null) {
            String phone = request.getPhoneNumber().trim();
            if (phone.isEmpty()) {
                user.setPhoneNumber(null);
            } else {
                if (usersRepository.existsByPhoneNumberExcludingId(phone, userId)) {
                    throw new ConflictException("phone_taken", "Телефон уже занят");
                }
                user.setPhoneNumber(phone);
            }
        }

        User updated = usersRepository.update(user);
        updated.setRoles(usersRepository.findRolesByUserId(userId));
        return userMapper.toUserResponse(updated);
    }

    public PublicUserResponse getUser(Long id) {
        User user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user_not_found", "Пользователь не найден"));
        return userMapper.toPublicUserResponse(user);
    }

    public ItemsResponse<PublicUserResponse> getUsersByIds(String idsParam) {
        if (idsParam == null || idsParam.isBlank()) {
            throw new BadRequestException("invalid_ids", "Параметр ids обязателен");
        }

        List<Long> ids;
        try {
            ids = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
        } catch (NumberFormatException ex) {
            throw new BadRequestException("invalid_ids", "ids должен содержать числовые идентификаторы");
        }

        if (ids.isEmpty()) {
            throw new BadRequestException("invalid_ids", "Параметр ids обязателен");
        }

        List<PublicUserResponse> items = usersRepository.findAllByIds(ids).stream()
                .map(userMapper::toPublicUserResponse)
                .toList();

        return new ItemsResponse<>(items);
    }
}
