package com.userservice.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.userservice.dao.UsersRepository;
import com.userservice.dto.LoginRequest;
import com.userservice.dto.LoginResponse;
import com.userservice.dto.RegisterRequest;
import com.userservice.dto.UserResponse;
import com.userservice.exception.BadRequestException;
import com.userservice.exception.ConflictException;
import com.userservice.exception.UnauthorizedException;
import com.userservice.model.Role;
import com.userservice.model.User;
import com.userservice.util.JwtProvider;
import com.userservice.util.UserMapper;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "subscriber";

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    public AuthService(
            UsersRepository usersRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            UserMapper userMapper) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        validateRegister(request);

        if (usersRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("email_taken", "Email уже занят");
        }

        String phone = normalizePhone(request.getPhoneNumber());
        if (phone != null && usersRepository.existsByPhoneNumber(phone)) {
            throw new ConflictException("phone_taken", "Телефон уже занят");
        }

        Role subscriber = usersRepository.findRoleByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role 'subscriber' is not seeded"));

        User user = new User();
        user.setFirstname(request.getFirstname().trim());
        user.setLastname(request.getLastname().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhoneNumber(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = usersRepository.save(user);
        usersRepository.saveUserRole(saved.getId(), subscriber.getId());
        saved.setRoles(usersRepository.findRolesByUserId(saved.getId()));

        return userMapper.toUserResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        if (isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("invalid_credentials", "Email и password обязательны");
        }

        User user = usersRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("invalid_login", "Неверный email или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("invalid_login", "Неверный email или пароль");
        }

        String token = jwtProvider.generate(user.getId(), user.getRoles());
        return new LoginResponse(token);
    }

    private void validateRegister(RegisterRequest request) {
        if (isBlank(request.getFirstname())
                || isBlank(request.getLastname())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {
            throw new BadRequestException("invalid_data", "firstname, lastname, email и password обязательны");
        }

        if (request.getPassword().length() < 6) {
            throw new BadRequestException("invalid_password", "Пароль должен быть не короче 6 символов");
        }

        if (!request.getEmail().contains("@")) {
            throw new BadRequestException("invalid_email", "Некорректный email");
        }
    }

    private static String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        String trimmed = phoneNumber.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
