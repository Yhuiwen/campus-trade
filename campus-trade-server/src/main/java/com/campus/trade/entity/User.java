package com.campus.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private Integer creditScore;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
