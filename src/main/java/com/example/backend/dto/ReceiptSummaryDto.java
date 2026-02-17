package com.example.backend.dto;

import com.example.backend.domain.receipt.ReceiptStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptSummaryDto {
  private Long id;
  private String storeName;
  private Integer totalAmount;
  private LocalDateTime tradeAt;
  private ReceiptStatus status;
  private String userName;
}
