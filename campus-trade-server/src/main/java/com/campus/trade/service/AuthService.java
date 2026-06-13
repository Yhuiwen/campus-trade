package com.campus.trade.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.dto.AuthDtos.LoginRequest;
import com.campus.trade.dto.AuthDtos.LoginResponse;
import com.campus.trade.dto.AuthDtos.RegisterRequest;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.security.JwtService;
import com.campus.trade.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (count > 0) throw new BusinessException("用户名已存在");
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setPhone(request.phone());
        user.setEmail(request.email());
        user.setRole("USER");
        user.setCreditScore(100);
        user.setStatus("NORMAL");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!"NORMAL".equals(user.getStatus())) throw new BusinessException(403, "账号已被封禁");
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(jwtService.generate(loginUser), user);
    }

    public User getUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }
}
