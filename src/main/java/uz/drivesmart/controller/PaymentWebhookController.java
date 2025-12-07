package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.service.PaymentService;

import java.util.Map;

/**
 * To'lov tizimlaridan webhook'larni qabul qilish
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Hidden // Swagger'da ko'rinmasin
public class PaymentWebhookController {

    private final PaymentService paymentService;

    /**
     * Click webhook
     * POST https://yourdomain.uz/api/webhooks/click
     */
    @PostMapping("/click")
    public ResponseEntity<Map<String, Object>> clickWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Click webhook received: {}", payload);

        try {
            String transactionId = (String) payload.get("merchant_trans_id");
            Integer action = (Integer) payload.get("action");

            // action == 1: To'lov muvaffaqiyatli
            if (action != null && action == 1) {
                paymentService.confirmPayment(transactionId);

                return ResponseEntity.ok(Map.of(
                        "error", 0,
                        "error_note", "Success"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "error", -1,
                    "error_note", "Invalid action"
            ));

        } catch (Exception e) {
            log.error("Click webhook error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "error", -1,
                    "error_note", e.getMessage()
            ));
        }
    }

    /**
     * Payme webhook
     * POST https://yourdomain.uz/api/webhooks/payme
     */
    @PostMapping("/payme")
    public ResponseEntity<Map<String, Object>> paymeWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Payme webhook received: {}", payload);

        try {
            Map<String, Object> params = (Map<String, Object>) payload.get("params");
            String transactionId = (String) params.get("id");
            String method = (String) payload.get("method");

            if ("PerformTransaction".equals(method)) {
                paymentService.confirmPayment(transactionId);

                return ResponseEntity.ok(Map.of(
                        "result", Map.of(
                                "transaction", transactionId,
                                "state", 2 // Success
                        )
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "error", Map.of(
                            "code", -32601,
                            "message", "Method not found"
                    )
            ));

        } catch (Exception e) {
            log.error("Payme webhook error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "error", Map.of(
                            "code", -32400,
                            "message", e.getMessage()
                    )
            ));
        }
    }

    /**
     * Uzum webhook
     * POST https://yourdomain.uz/api/webhooks/uzum
     */
    @PostMapping("/uzum")
    public ResponseEntity<Map<String, Object>> uzumWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Uzum webhook received: {}", payload);

        try {
            String transactionId = (String) payload.get("transaction_id");
            String status = (String) payload.get("status");

            if ("success".equals(status)) {
                paymentService.confirmPayment(transactionId);

                return ResponseEntity.ok(Map.of(
                        "status", "ok"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", "Invalid status"
            ));

        } catch (Exception e) {
            log.error("Uzum webhook error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
