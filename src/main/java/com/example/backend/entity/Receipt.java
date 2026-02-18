package com.example.backend.entity;

import com.example.backend.domain.receipt.ReceiptStatus;
import com.example.backend.domain.receipt.SystemErrorCode;
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

  @Enumerated(EnumType.STRING)
  private SystemErrorCode systemErrorCode;

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

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "receipt_tags", joinColumns = @JoinColumn(name = "receipt_id"))
  @Column(name = "tag_name")
  @Builder.Default
  private java.util.List<String> tags = new java.util.ArrayList<>();

  public void updateTags(java.util.List<String> newTags) {
    this.tags.clear();
    if (newTags != null) {
      this.tags.addAll(newTags);
    }
  }

  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    if (this.status == null) {
      this.status = ReceiptStatus.ANALYZING;
    }
  }

  public void updateStatus(ReceiptStatus status, String reason) {
    if (this.status == ReceiptStatus.APPROVED && status != ReceiptStatus.APPROVED) {
      throw new IllegalStateException("이미 승인된 영수증의 상태를 변경할 수 없습니다.");
    }

    this.status = status;
    if (status == ReceiptStatus.REJECTED) {
      this.rejectionReason = reason;
    }
  }

  public void updateSystemError(SystemErrorCode errorCode) {
    this.status = ReceiptStatus.FAILED_SYSTEM;
    this.systemErrorCode = errorCode;
  }

  public void updateInfo(Integer totalAmount, String storeName, LocalDateTime tradeAt) {
    if (this.status == ReceiptStatus.APPROVED) {
      throw new IllegalStateException("이미 승인된 영수증은 수정할 수 없습니다.");
    }

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
    this.systemErrorCode = null;
  }

  public void resubmit() {
    if (this.status != ReceiptStatus.REJECTED) {
      throw new IllegalStateException("반려된 상태의 영수증만 재제출이 가능합니다.");
    }
    this.status = ReceiptStatus.WAITING;
    this.rejectionReason = null;
  }
}
