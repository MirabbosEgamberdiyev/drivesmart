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

@Service
@Slf4j
public class PaymePaymentService {

    @Value("${app.payment.payme.merchant-id}")
    private String merchantId;

    @Value("${app.payment.payme.key}")
    private String merchantKey;

    @Value("${app.payment.payme.api-url:https://checkout.paycom.uz/api}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PaymePaymentService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * ✅ Payme invoice yaratish
     */
    public String createInvoice(UserPurchase purchase) {
        try {
            log.info("Creating Payme invoice for purchase: {}", purchase.getId());

            // Payme uses base64 encoded merchant parameters
            String params = String.format("m=%s;ac.order_id=%s;a=%s",
                    merchantId,
                    purchase.getId(),
                    purchase.getAmount().multiply(new java.math.BigDecimal("100")).longValue()
            );

            String encodedParams = Base64.getEncoder().encodeToString(
                    params.getBytes(StandardCharsets.UTF_8)
            );

            String invoiceId = "PAYME_" + System.currentTimeMillis();
            log.info("Payme invoice created: {}", invoiceId);

            return encodedParams;

        } catch (Exception e) {
            log.error("Payme API error: {}", e.getMessage(), e);
            throw new BusinessException("To'lov tizimi bilan bog'lanishda xatolik");
        }
    }

    /**
     * ✅ To'lov URL'ini olish
     */
    public String getPaymentUrl(String encodedParams) {
        return "https://checkout.paycom.uz/" + encodedParams;
    }

    /**
     * ✅ Webhook signature verification
     */
    public boolean verifyWebhookSignature(HttpHeaders headers) {
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }

        String credentials = new String(
                Base64.getDecoder().decode(authHeader.substring(6)),
                StandardCharsets.UTF_8
        );

        String[] parts = credentials.split(":");
        return parts.length == 2 &&
                parts[0].equals("Paycom") &&
                parts[1].equals(merchantKey);
    }
}
