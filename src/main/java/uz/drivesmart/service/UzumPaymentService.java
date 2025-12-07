package uz.drivesmart.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.drivesmart.entity.UserPurchase;


@Service
@Slf4j
public class UzumPaymentService {

    @Value("${app.payment.uzum.merchant-id}")
    private String merchantId;

    public String createInvoice(UserPurchase purchase) {
        log.info("Creating Uzum invoice for purchase: {}", purchase.getId());
        return "UZUM_" + System.currentTimeMillis();
    }

    public String getPaymentUrl(String transactionId) {
        return "https://pay.uzum.uz/checkout/" + transactionId;
    }
}
