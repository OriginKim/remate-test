package com.example.backend.controller;

import com.example.backend.entity.WorkspaceMember;
import com.example.backend.service.WorkspaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  @PostMapping("/{workspaceId}/join")
  public ResponseEntity<WorkspaceMember> requestJoin(@PathVariable Long workspaceId) {
    return ResponseEntity.ok(workspaceService.requestJoin(workspaceId, getCurrentUserId()));
  }

  @GetMapping("/{workspaceId}/pending")
  public ResponseEntity<List<WorkspaceMember>> getPendingMembers(@PathVariable Long workspaceId) {
    return ResponseEntity.ok(workspaceService.getPendingMembers(workspaceId));
  }

  @PostMapping("/members/{membershipId}/approve")
  public ResponseEntity<Void> approve(@PathVariable Long membershipId) {
    workspaceService.approveMembership(membershipId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/members/{membershipId}/reject")
  public ResponseEntity<Void> reject(@PathVariable Long membershipId) {
    workspaceService.rejectMembership(membershipId);
    return ResponseEntity.ok().build();
  }

  private Long getCurrentUserId() {
    String principal = SecurityContextHolder.getContext().getAuthentication().getName();
    return Long.parseLong(principal);
  }
}
