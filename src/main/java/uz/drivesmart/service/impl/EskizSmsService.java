package uz.drivesmart.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.drivesmart.exception.BusinessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Eskiz.uz SMS xizmati
 */
@Service
@Slf4j
public class EskizSmsService {

    @Value("${app.sms.eskiz.email}")
    private String eskizEmail;

    @Value("${app.sms.eskiz.secret-key}")
    private String eskizSecretKey;

    @Value("${app.sms.eskiz.test-mode:true}")
    private boolean testMode;

    private static final String BASE_URL = "https://notify.eskiz.uz/api";
    private String cachedToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EskizSmsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * SMS yuborish
     *
     * @param phoneNumber Telefon raqam (998901234567)
     * @param message SMS matni
     */
    public void sendSms(String phoneNumber, String message) {
        if (testMode) {
            log.info("TEST MODE: SMS yuborilmadi. Telefon: {}, Xabar: {}", phoneNumber, message);
            return;
        }

        try {
            String token = getAuthToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, String> body = new HashMap<>();
            body.put("mobile_phone", phoneNumber);
            body.put("message", message);
            body.put("from", "4546"); // Default sender

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/message/sms/send",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("SMS muvaffaqiyatli yuborildi: {}", phoneNumber);
            } else {
                log.error("SMS yuborishda xatolik: {}", response.getBody());
                throw new BusinessException("SMS yuborishda xatolik yuz berdi");
            }

        } catch (Exception e) {
            log.error("SMS yuborishda xatolik: {}", e.getMessage());
            throw new BusinessException("SMS yuborishda xatolik: " + e.getMessage());
        }
    }

    /**
     * Auth token olish
     */
    private String getAuthToken() {
        if (cachedToken != null) {
            return cachedToken;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("email", eskizEmail);
            body.put("password", eskizSecretKey);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/auth/login",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                cachedToken = jsonNode.path("data").path("token").asText();
                log.info("Eskiz token olindi");
                return cachedToken;
            } else {
                throw new BusinessException("Eskiz autentifikatsiyasida xatolik");
            }

        } catch (Exception e) {
            log.error("Eskiz token olishda xatolik: {}", e.getMessage());
            throw new BusinessException("SMS xizmati ishlamayapti");
        }
    }
}