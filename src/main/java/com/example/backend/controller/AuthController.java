package com.example.backend.controller;

import com.example.backend.dto.AuthStatusResponse;
import com.example.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/status")
  public ResponseEntity<AuthStatusResponse> getStatus() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getName())) {
      return ResponseEntity.ok(
          new AuthStatusResponse(false, null, "미인증 상태", null, null, null, null));
    }

    return ResponseEntity.ok(authService.getAuthStatusByPrincipal(authentication.getName()));
  }
}
