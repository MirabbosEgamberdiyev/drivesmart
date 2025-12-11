package uz.drivesmart.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.drivesmart.entity.UserPurchase;
import uz.drivesmart.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ClickPaymentService {

    @Value("${app.payment.click.merchant-id}")
    private String merchantId;

    @Value("${app.payment.click.service-id}")
    private String serviceId;

    @Value("${app.payment.click.secret-key}")
    private String secretKey;

    @Value("${app.payment.click.api-url:https://api.click.uz/v2}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ClickPaymentService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * ✅ Invoice yaratish (Click API orqali)
     */
    public String createInvoice(UserPurchase purchase) {
        try {
            log.info("Creating Click invoice for purchase: {}", purchase.getId());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("service_id", serviceId);
            requestBody.put("merchant_id", merchantId);
            requestBody.put("amount", purchase.getAmount().doubleValue());
            requestBody.put("transaction_param", purchase.getId().toString());
            requestBody.put("merchant_trans_id", "TXN_" + System.currentTimeMillis());
            requestBody.put("return_url", "https://yourdomain.uz/payment/success");
            requestBody.put("cancel_url", "https://yourdomain.uz/payment/cancel");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + getAuthHeader());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/invoices",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String invoiceId = (String) response.getBody().get("invoice_id");
                log.info("Click invoice created: {}", invoiceId);
                return invoiceId;
            } else {
                throw new BusinessException("Click invoice yaratishda xatolik");
            }

        } catch (Exception e) {
            log.error("Click API error: {}", e.getMessage(), e);
            throw new BusinessException("To'lov tizimi bilan bog'lanishda xatolik");
        }
    }

    /**
     * ✅ To'lov URL'ini olish
     */
    public String getPaymentUrl(String invoiceId) {
        return String.format(
                "https://my.click.uz/services/pay?service_id=%s&merchant_id=%s&transaction_param=%s",
                serviceId, merchantId, invoiceId
        );
    }

    /**
     * ✅ Webhook signature verification
     */
    public boolean verifyWebhookSignature(Map<String, Object> payload, String receivedSignature) {
        try {
            // Click webhook signature format:
            // MD5(click_trans_id + service_id + secret_key + merchant_trans_id + amount + action + sign_time)
            String signString = String.format("%s%s%s%s%s%s%s",
                    payload.get("click_trans_id"),
                    serviceId,
                    secretKey,
                    payload.get("merchant_trans_id"),
                    payload.get("amount"),
                    payload.get("action"),
                    payload.get("sign_time")
            );

            String calculatedSignature = generateMD5(signString);
            return calculatedSignature.equals(receivedSignature);

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }

    private String getAuthHeader() {
        String credentials = merchantId + ":" + secretKey;
        return Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String generateMD5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 generation failed", e);
        }
    }
}