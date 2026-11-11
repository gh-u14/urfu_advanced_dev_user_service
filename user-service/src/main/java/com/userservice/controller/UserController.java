package com.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.userservice.config.AuthContext;
import com.userservice.dto.ItemsResponse;
import com.userservice.dto.PublicUserResponse;
import com.userservice.dto.UpdateUserRequest;
import com.userservice.dto.UserResponse;
import com.userservice.service.UserService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Mono<UserResponse> getMe(ServerWebExchange exchange) {
        Long userId = AuthContext.requireUserId(exchange);
        return Mono.fromCallable(() -> userService.getMe(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping("/me")
    public Mono<UserResponse> updateMe(
            ServerWebExchange exchange,
            @RequestBody UpdateUserRequest request) {
        Long userId = AuthContext.requireUserId(exchange);
        return Mono.fromCallable(() -> userService.updateMe(userId, request))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<PublicUserResponse> getUser(@PathVariable Long id) {
        return Mono.fromCallable(() -> userService.getUser(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping
    public Mono<ResponseEntity<ItemsResponse<PublicUserResponse>>> getUsersByIds(
            @RequestParam("ids") String ids) {
        return Mono.fromCallable(() -> userService.getUsersByIds(ids))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}
