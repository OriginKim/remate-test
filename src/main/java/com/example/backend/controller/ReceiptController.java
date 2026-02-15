package com.example.backend.controller;

import com.example.backend.domain.receipt.ReceiptStatus;
import com.example.backend.entity.AuditLog;
import com.example.backend.entity.Receipt;
import com.example.backend.service.AuditLogService;
import com.example.backend.service.ReceiptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReceiptController {

  private final ReceiptService receiptService;
  private final AuditLogService auditLogService;

  @GetMapping
  public ResponseEntity<List<Receipt>> getAllReceipts() {
    return ResponseEntity.ok(receiptService.getReceipts(1L, true));
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> exportToCsv() {
    try {
      List<Receipt> receipts = receiptService.getReceipts(1L, true);
      byte[] out = receiptService.generateCsv(receipts);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_list.csv")
          .body(out);
    } catch (Exception e) {
      log.error("CSV 생성 실패", e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> upload(
      @RequestHeader("X-IDEMPOTENCY-KEY") String idempotencyKey,
      @RequestPart("file") MultipartFile file,
      @RequestParam("workspaceId") Long workspaceId,
      @RequestParam("userId") Long userId) {
    if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("파일이 없습니다.");

    try {
      Receipt receipt = receiptService.uploadAndProcess(idempotencyKey, file, workspaceId, userId);
      return ResponseEntity.ok(receipt);
    } catch (Exception e) {
      log.error("업로드 실패", e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<Receipt> updateStatus(
      @PathVariable Long id,
      @RequestParam ReceiptStatus status,
      @RequestParam(required = false, defaultValue = "관리자 요청") String reason) {
    return ResponseEntity.ok(receiptService.updateStatus(id, 1L, status, reason));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Receipt> updateReceipt(
      @PathVariable Long id, @RequestBody java.util.Map<String, Object> payload) {

    String storeName = (String) payload.get("storeName");
    Integer totalAmount = (Integer) payload.get("totalAmount");

    return ResponseEntity.ok(
        receiptService.updateReceipt(
            id, 1L, totalAmount, storeName, java.time.LocalDateTime.now()));
  }

  @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<Receipt>> uploadMultiple(
      @RequestPart("files") List<MultipartFile> files,
      @RequestPart("workspaceId") String workspaceId,
      @RequestPart("userId") String userId) {
    if (files == null || files.isEmpty()) return ResponseEntity.badRequest().build();

    Long wId = Long.parseLong(workspaceId);
    Long uId = Long.parseLong(userId);

    List<Receipt> results = receiptService.uploadMultiple(files, wId, uId);
    return ResponseEntity.ok(results);
  }

  @GetMapping("/{id}/history")
  public ResponseEntity<List<AuditLog>> getHistory(@PathVariable Long id) {
    try {
      List<AuditLog> logs = auditLogService.getHistory(id);
      return ResponseEntity.ok(logs);
    } catch (Exception e) {
      log.error("이력 조회 실패 - receiptId: {}", id, e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
