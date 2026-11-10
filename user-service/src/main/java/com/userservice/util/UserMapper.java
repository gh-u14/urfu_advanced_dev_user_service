package com.userservice.util;

import org.springframework.stereotype.Component;

import com.userservice.dto.UserResponse;
import com.userservice.model.User;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRoles());
    }
}
