package com.example.backend.entity;

import com.example.backend.domain.receipt.ReceiptStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "receipt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Receipt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReceiptStatus status;

  private String storeName;

  private LocalDateTime tradeAt;

  private int totalAmount;

  @Builder.Default
  @Column(name = "night_time", columnDefinition = "TINYINT(1)")
  private boolean nightTime = false;

  @Column(unique = true)
  private String idempotencyKey;

  @Column(name = "file_hash", unique = true)
  private String fileHash;

  private String filePath;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String rawText;

  @Column(nullable = false)
  private Long workspaceId;

  @Column(nullable = false)
  private Long userId;

  private String rejectionReason;

  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    if (this.status == null) {
      this.status = ReceiptStatus.ANALYZING;
    }
  }

  public void updateStatus(ReceiptStatus status, String reason) {
    this.status = status;
    if (status == ReceiptStatus.REJECTED) {
      this.rejectionReason = reason;
    }
  }

  public void updateInfo(Integer totalAmount, String storeName, LocalDateTime tradeAt) {
    if (totalAmount != null) {
      this.totalAmount = totalAmount;
    }
    if (storeName != null && !storeName.isEmpty()) {
      this.storeName = storeName;
    }
    if (tradeAt != null) {
      this.tradeAt = tradeAt;
      this.nightTime = (tradeAt.getHour() >= 23 || tradeAt.getHour() < 6);
    }
    this.status = ReceiptStatus.APPROVED;
  }
}
