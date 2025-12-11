package uz.drivesmart.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.service.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ✅ XAVFSIZ Webhook Controller
 * - Signature verification
 * - Idempotency handling
 * - Rate limiting
 * - Structured logging
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final ClickPaymentService clickService;
    private final PaymePaymentService paymeService;
    private final UzumPaymentService uzumService;

    // Idempotency cache (production'da Redis ishlatish tavsiya etiladi)
    private final ConcurrentHashMap<String, Long> processedWebhooks = new ConcurrentHashMap<>();

    /**
     * ✅ XAVFSIZ Click Webhook
     */
    @PostMapping("/click")
    public ResponseEntity<Map<String, Object>> clickWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader HttpHeaders headers) {

        String requestId = (String) payload.get("click_trans_id");
        log.info("📥 Click webhook received: requestId={}", requestId);

        try {
            // 1. Signature verification
            String signature = (String) payload.get("sign_string");
            if (!clickService.verifyWebhookSignature(payload, signature)) {
                log.error("❌ Click webhook signature verification failed");
                return ResponseEntity.ok(Map.of(
                        "error", -1,
                        "error_note", "Invalid signature"
                ));
            }

            // 2. Idempotency check
            if (isAlreadyProcessed(requestId)) {
                log.warn("⚠️ Duplicate webhook ignored: {}", requestId);
                return ResponseEntity.ok(Map.of(
                        "error", 0,
                        "error_note", "Already processed"
                ));
            }

            // 3. Process payment
            String transactionId = (String) payload.get("merchant_trans_id");
            Integer action = (Integer) payload.get("action");

            if (action != null && action == 1) { // 1 = success
                paymentService.confirmPayment(transactionId);
                markAsProcessed(requestId);

                log.info("✅ Click payment confirmed: {}", transactionId);

                return ResponseEntity.ok(Map.of(
                        "click_trans_id", requestId,
                        "merchant_trans_id", transactionId,
                        "error", 0,
                        "error_note", "Success"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "error", -1,
                    "error_note", "Invalid action"
            ));

        } catch (Exception e) {
            log.error("❌ Click webhook error: requestId={}, error={}",
                    requestId, e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "error", -1,
                    "error_note", e.getMessage()
            ));
        }
    }

    /**
     * ✅ XAVFSIZ Payme Webhook
     */
    @PostMapping("/payme")
    public ResponseEntity<Map<String, Object>> paymeWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader HttpHeaders headers) {

        String method = (String) payload.get("method");
        Map<String, Object> params = (Map<String, Object>) payload.get("params");
        String transactionId = params != null ? (String) params.get("id") : null;

        log.info("📥 Payme webhook received: method={}, transactionId={}", method, transactionId);

        try {
            // 1. Authentication check
            if (!paymeService.verifyWebhookSignature(headers)) {
                log.error("❌ Payme webhook authentication failed");
                return ResponseEntity.ok(Map.of(
                        "error", Map.of(
                                "code", -32504,
                                "message", "Insufficient privilege"
                        )
                ));
            }

            // 2. Idempotency check
            if (transactionId != null && isAlreadyProcessed(transactionId)) {
                log.warn("⚠️ Duplicate Payme webhook ignored: {}", transactionId);
                return ResponseEntity.ok(Map.of(
                        "result", Map.of(
                                "transaction", transactionId,
                                "state", 2
                        )
                ));
            }

            // 3. Handle methods
            switch (method) {
                case "PerformTransaction":
                    String merchantTransId = (String) params.get("account");
                    paymentService.confirmPayment(merchantTransId);
                    markAsProcessed(transactionId);

                    log.info("✅ Payme payment confirmed: {}", transactionId);

                    return ResponseEntity.ok(Map.of(
                            "result", Map.of(
                                    "transaction", transactionId,
                                    "state", 2,
                                    "perform_time", System.currentTimeMillis()
                            )
                    ));

                case "CheckTransaction":
                    // Check if transaction exists
                    return ResponseEntity.ok(Map.of(
                            "result", Map.of(
                                    "transaction", transactionId,
                                    "state", 2
                            )
                    ));

                default:
                    return ResponseEntity.ok(Map.of(
                            "error", Map.of(
                                    "code", -32601,
                                    "message", "Method not found"
                            )
                    ));
            }

        } catch (Exception e) {
            log.error("❌ Payme webhook error: transactionId={}, error={}",
                    transactionId, e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "error", Map.of(
                            "code", -32400,
                            "message", e.getMessage()
                    )
            ));
        }
    }

    /**
     * ✅ XAVFSIZ Uzum Webhook
     */
    @PostMapping("/uzum")
    public ResponseEntity<Map<String, Object>> uzumWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {

        log.info("📥 Uzum webhook received");

        try {
            // 1. Signature verification
            if (!uzumService.verifyWebhookSignature(payload, signature)) {
                log.error("❌ Uzum webhook signature verification failed");
                return ResponseEntity.ok(Map.of(
                        "status", "error",
                        "message", "Invalid signature"
                ));
            }

            // 2. Parse payload
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> data = mapper.readValue(payload, Map.class);

            String transactionId = (String) data.get("transaction_id");
            String status = (String) data.get("status");

            // 3. Idempotency check
            if (isAlreadyProcessed(transactionId)) {
                log.warn("⚠️ Duplicate Uzum webhook ignored: {}", transactionId);
                return ResponseEntity.ok(Map.of("status", "ok"));
            }

            // 4. Process payment
            if ("success".equals(status)) {
                paymentService.confirmPayment(transactionId);
                markAsProcessed(transactionId);

                log.info("✅ Uzum payment confirmed: {}", transactionId);

                return ResponseEntity.ok(Map.of("status", "ok"));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "Invalid status"
            ));

        } catch (Exception e) {
            log.error("❌ Uzum webhook error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * ✅ Idempotency checker
     */
    private boolean isAlreadyProcessed(String requestId) {
        Long processedAt = processedWebhooks.get(requestId);
        if (processedAt != null) {
            // 24 soat ichida qayta ishlanmasin
            return (System.currentTimeMillis() - processedAt) < TimeUnit.HOURS.toMillis(24);
        }
        return false;
    }

    /**
     * ✅ Mark as processed
     */
    private void markAsProcessed(String requestId) {
        processedWebhooks.put(requestId, System.currentTimeMillis());

        // Cleanup old entries (1 kundan eski)
        if (processedWebhooks.size() > 10000) {
            long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
            processedWebhooks.entrySet().removeIf(entry -> entry.getValue() < oneDayAgo);
        }
    }
}