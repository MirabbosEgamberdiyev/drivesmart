package uz.drivesmart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.drivesmart.entity.UserPurchase;
import uz.drivesmart.exception.BusinessException;

@Service
@Slf4j
public class ClickPaymentService {

    @Value("${app.payment.click.merchant-id}")
    private String merchantId;

    @Value("${app.payment.click.service-id}")
    private String serviceId;

    @Value("${app.payment.click.secret-key}")
    private String secretKey;

    public String createInvoice(UserPurchase purchase) {
        // TODO: Click API integration
        log.info("Creating Click invoice for purchase: {}", purchase.getId());
        return "CLICK_" + System.currentTimeMillis();
    }

    public String getPaymentUrl(String transactionId) {
        // TODO: Generate Click payment URL
        return "https://my.click.uz/services/pay?service_id=" + serviceId +
                "&merchant_id=" + merchantId + "&transaction_param=" + transactionId;
    }
}
