package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.dto.AuthDtos.LoginRequest;
import com.campus.trade.dto.AuthDtos.LoginResponse;
import com.campus.trade.dto.AuthDtos.RegisterRequest;
import com.campus.trade.entity.User;
import com.campus.trade.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.success(authService.getUser(CurrentUser.id()));
    }
}
