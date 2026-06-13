package com.campus.trade.dto;

import com.campus.trade.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "用户名不能为空") @Size(min = 3, max = 30) String username,
            @NotBlank(message = "密码不能为空") @Size(min = 6, max = 50) String password,
            @NotBlank(message = "昵称不能为空")
            @Size(max = 100, message = "昵称长度不能超过100个字符") String nickname,
            String phone, String email) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String token, User user) {}
}
