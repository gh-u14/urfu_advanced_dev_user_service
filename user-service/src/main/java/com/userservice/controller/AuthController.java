package com.userservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.userservice.dto.LoginRequest;
import com.userservice.dto.LoginResponse;
import com.userservice.dto.RegisterRequest;
import com.userservice.dto.UserResponse;
import com.userservice.service.AuthService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@RequestBody RegisterRequest request) {
        return Mono.fromCallable(() -> authService.register(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> authService.login(request))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok);
    }
}
