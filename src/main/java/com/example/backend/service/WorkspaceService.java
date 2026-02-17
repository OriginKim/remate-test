package com.example.backend.service;

import com.example.backend.entity.MembershipStatus;
import com.example.backend.entity.WorkspaceMember;
import com.example.backend.entity.WorkspaceRole;
import com.example.backend.repository.WorkspaceMemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

  private final WorkspaceMemberRepository workspaceMemberRepository;

  @Transactional
  public WorkspaceMember requestJoin(Long workspaceId, Long userId) {
    workspaceMemberRepository
        .findByWorkspaceIdAndUserId(workspaceId, userId)
        .ifPresent(
            m -> {
              throw new RuntimeException("ALREADY_JOINED_OR_PENDING");
            });

    return workspaceMemberRepository.save(
        WorkspaceMember.builder()
            .workspaceId(workspaceId)
            .userId(userId)
            .role(WorkspaceRole.MEMBER)
            .status(MembershipStatus.PENDING)
            .build());
  }

  @Transactional
  public void approveMembership(Long membershipId) {
    WorkspaceMember member =
        workspaceMemberRepository
            .findById(membershipId)
            .orElseThrow(() -> new RuntimeException("MEMBERSHIP_NOT_FOUND"));

    member.updateStatus(MembershipStatus.ACCEPTED);
  }

  @Transactional
  public void rejectMembership(Long membershipId) {
    WorkspaceMember member =
        workspaceMemberRepository
            .findById(membershipId)
            .orElseThrow(() -> new RuntimeException("MEMBERSHIP_NOT_FOUND"));

    member.updateStatus(MembershipStatus.REJECTED);
  }

  public List<WorkspaceMember> getPendingMembers(Long workspaceId) {
    return workspaceMemberRepository.findAllByWorkspaceId(workspaceId).stream()
        .filter(m -> m.getStatus() == MembershipStatus.PENDING)
        .toList();
  }
}
