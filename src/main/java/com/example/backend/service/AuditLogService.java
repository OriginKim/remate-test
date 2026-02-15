package com.example.backend.service;

import com.example.backend.domain.receipt.ReceiptStatus;
import com.example.backend.entity.AuditLog;
import com.example.backend.repository.AuditLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Transactional
  public void logStatusChange(
      Long receiptId,
      Long userId,
      ReceiptStatus oldStatus,
      ReceiptStatus newStatus,
      String reason) {
    AuditLog log =
        AuditLog.builder()
            .receiptId(receiptId)
            .userId(userId)
            .oldStatus(oldStatus)
            .newStatus(newStatus)
            .reason(reason)
            .build();

    auditLogRepository.save(log);
  }

  @Transactional(readOnly = true)
  public List<AuditLog> getHistory(Long receiptId) {
    return auditLogRepository.findAllByReceiptIdOrderByCreatedAtDesc(receiptId);
  }
}
