package uz.drivesmart.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.drivesmart.entity.UserPurchase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.drivesmart.entity.UserPurchase;
import uz.drivesmart.exception.BusinessException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
@Service
@Slf4j
public class UzumPaymentService {

    @Value("${app.payment.uzum.merchant-id}")
    private String merchantId;

    @Value("${app.payment.uzum.secret-key}")
    private String secretKey;

    @Value("${app.payment.uzum.api-url:https://api.uzum.uz/v1}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UzumPaymentService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * ✅ Uzum invoice yaratish
     */
    public String createInvoice(UserPurchase purchase) {
        try {
            log.info("Creating Uzum invoice for purchase: {}", purchase.getId());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchant_id", merchantId);
            requestBody.put("amount", purchase.getAmount().doubleValue());
            requestBody.put("order_id", purchase.getId().toString());
            requestBody.put("description", purchase.getTestPackage().getName());
            requestBody.put("callback_url", "https://yourdomain.uz/api/webhooks/uzum");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + generateToken());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/payments",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String transactionId = (String) response.getBody().get("transaction_id");
                log.info("Uzum invoice created: {}", transactionId);
                return transactionId;
            } else {
                throw new BusinessException("Uzum invoice yaratishda xatolik");
            }

        } catch (Exception e) {
            log.error("Uzum API error: {}", e.getMessage(), e);
            throw new BusinessException("To'lov tizimi bilan bog'lanishda xatolik");
        }
    }

    /**
     * ✅ To'lov URL'ini olish
     */
    public String getPaymentUrl(String transactionId) {
        return "https://pay.uzum.uz/checkout/" + transactionId;
    }

    /**
     * ✅ Webhook signature verification
     */
    public boolean verifyWebhookSignature(String payload, String receivedSignature) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKeySpec);

            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);

            return calculatedSignature.equals(receivedSignature);

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String generateToken() {
        // JWT token generation logic
        // Uzum API documentation'ga muvofiq implement qiling
        return "your_jwt_token_here";
    }
}