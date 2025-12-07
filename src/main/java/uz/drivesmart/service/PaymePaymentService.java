package uz.drivesmart.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.drivesmart.entity.UserPurchase;

@Service
@Slf4j
public class PaymePaymentService {

    @Value("${app.payment.payme.merchant-id}")
    private String merchantId;

    public String createInvoice(UserPurchase purchase) {
        log.info("Creating Payme invoice for purchase: {}", purchase.getId());
        return "PAYME_" + System.currentTimeMillis();
    }

    public String getPaymentUrl(String transactionId) {
        return "https://checkout.paycom.uz/" + merchantId;
    }
}
