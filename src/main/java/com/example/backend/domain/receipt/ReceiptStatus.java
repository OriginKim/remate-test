package com.example.backend.domain.receipt;

import java.util.List;
import java.util.Map;

public enum ReceiptStatus {
  UPLOADED,
  ANALYZING,
  WAITING,
  NEED_MANUAL,
  APPROVED,
  REJECTED,
  FAILED_SYSTEM,
  EXPIRED;

  private static final Map<ReceiptStatus, List<ReceiptStatus>> TRANSITION_TABLE =
      Map.of(
          ANALYZING, List.of(WAITING, NEED_MANUAL, FAILED_SYSTEM),
          WAITING, List.of(APPROVED, REJECTED, EXPIRED),
          NEED_MANUAL, List.of(WAITING, APPROVED, REJECTED),
          REJECTED, List.of(WAITING),
          APPROVED, List.of());

  public boolean canTransitionTo(ReceiptStatus nextStatus) {
    if (this == nextStatus) return true;
    List<String> allowed =
        TRANSITION_TABLE.getOrDefault(this, List.of()).stream().map(Enum::name).toList();
    return TRANSITION_TABLE.getOrDefault(this, List.of()).contains(nextStatus);
  }
}
