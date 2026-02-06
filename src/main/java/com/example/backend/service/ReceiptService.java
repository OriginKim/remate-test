package com.example.backend.service;

import com.example.backend.domain.receipt.ReceiptStatus;
import com.example.backend.entity.Receipt;
import com.example.backend.ocr.GoogleOcrClient;
import com.example.backend.repository.ReceiptRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final GoogleOcrClient googleOcrClient;

    @Transactional
    public Receipt uploadAndProcess(String idempotencyKey, MultipartFile file, Long workspaceId, Long userId) {
        validateFile(file);


        return receiptRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> {
                    try {
                        JsonNode ocrJson = googleOcrClient.recognize(file.getBytes());

                        Receipt newReceipt = Receipt.builder()
                                .workspaceId(workspaceId)
                                .userId(userId)
                                .idempotencyKey(idempotencyKey)
                                .rawText(ocrJson.toString())
                                .status(ReceiptStatus.ANALYZING)

                                .build();

                        return receiptRepository.save(newReceipt);
                    } catch (Exception e) {
                        throw new RuntimeException("OCR_PROCESSING_FAILED");
                    }
                });
    }

    private void validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("application/pdf"))) {
            throw new RuntimeException("FILE_TYPE_NOT_ALLOWED");
        }
    }
}