package com.campus.trade.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtSecretValidator implements ApplicationRunner {
  private static final String DEFAULT_SECRET = "campus-trade-demo-secret-change-in-production-2026";
  private static final int MIN_SECRET_LENGTH = 32;

  @Value("${jwt.secret}")
  private String secret;

  @Override
  public void run(ApplicationArguments args) {
    if (secret == null || secret.length() < MIN_SECRET_LENGTH) {
      throw new IllegalStateException(
          "JWT_SECRET 长度不足 " + MIN_SECRET_LENGTH + " 字符，请通过环境变量设置更长的密钥");
    }
    if (DEFAULT_SECRET.equals(secret)) {
      log.warn("正在使用默认 JWT_SECRET，仅供本地开发；生产环境必须通过环境变量 JWT_SECRET 替换");
    }
  }
}
