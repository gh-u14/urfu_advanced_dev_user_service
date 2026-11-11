package com.userservice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.userservice.config.AuthContext;
import com.userservice.service.RoleService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/users")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PutMapping("/{id}/roles/{role}")
    public Mono<ResponseEntity<Void>> assignRole(
            ServerWebExchange exchange,
            @PathVariable("id") Long userId,
            @PathVariable("role") String role) {
        Long actorId = AuthContext.requireUserId(exchange);
        List<String> roles = AuthContext.roles(exchange);

        return Mono.fromRunnable(() -> roleService.assignRole(actorId, roles, userId, role))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @DeleteMapping("/{id}/roles/{role}")
    public Mono<ResponseEntity<Void>> revokeRole(
            ServerWebExchange exchange,
            @PathVariable("id") Long userId,
            @PathVariable("role") String role) {
        Long actorId = AuthContext.requireUserId(exchange);
        List<String> roles = AuthContext.roles(exchange);

        return Mono.fromRunnable(() -> roleService.revokeRole(actorId, roles, userId, role))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
