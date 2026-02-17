package com.example.backend.service;

import com.example.backend.domain.User;
import com.example.backend.dto.AuthStatusResponse;
import com.example.backend.entity.MembershipStatus;
import com.example.backend.entity.WorkspaceMember;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final WorkspaceMemberRepository workspaceMemberRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Transactional
  public Long join(String email, String rawPassword, String name) {
    String encodedPassword = passwordEncoder.encode(rawPassword);
    User user =
        User.builder().email(email).password(encodedPassword).name(name).provider("local").build();
    return userRepository.save(user).getId();
  }

  @Transactional(readOnly = true)
  public AuthStatusResponse getAuthStatusByPrincipal(String principal) {
    User user =
        userRepository
            .findByEmail(principal)
            .orElseGet(
                () ->
                    userRepository.findAll().stream()
                        .filter(u -> principal.equals(u.getProviderId()))
                        .findFirst()
                        .orElse(null));

    if (user == null) {
      return new AuthStatusResponse(false, null, "유저를 찾을 수 없습니다.", null, null, null, null);
    }

    WorkspaceMember membership =
        workspaceMemberRepository.findAll().stream()
            .filter(
                m ->
                    m.getUserId().equals(user.getId())
                        && m.getStatus() == MembershipStatus.ACCEPTED)
            .findFirst()
            .orElse(null);

    Long workspaceId = (membership != null) ? membership.getWorkspaceId() : null;
    com.example.backend.entity.WorkspaceRole role =
        (membership != null) ? membership.getRole() : null;

    // 마지막 인자로 user.getId()를 추가하여 프론트엔드에 전달합니다.
    return new AuthStatusResponse(
        true, user.getEmail(), "인증 성공", workspaceId, user.getName(), role, user.getId());
  }
}
