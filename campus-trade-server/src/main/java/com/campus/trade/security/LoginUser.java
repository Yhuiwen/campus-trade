package com.campus.trade.security;

import java.io.Serializable;

public record LoginUser(Long id, String username, String role) implements Serializable {}
