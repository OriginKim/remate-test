package com.example.backend.entity;

import com.example.backend.domain.receipt.ReceiptStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long receiptId;

  @Column(nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReceiptStatus oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReceiptStatus newStatus;

  private String reason;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public AuditLog(
      Long receiptId,
      Long userId,
      ReceiptStatus oldStatus,
      ReceiptStatus newStatus,
      String reason) {
    this.receiptId = receiptId;
    this.userId = userId;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.reason = reason;
  }
}
