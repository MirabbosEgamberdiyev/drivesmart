package uz.drivesmart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.request.CardTransferRequest;
import uz.drivesmart.dto.request.PurchaseInitRequest;
import uz.drivesmart.dto.response.PurchaseResponse;
import uz.drivesmart.entity.TestPackage;
import uz.drivesmart.entity.User;
import uz.drivesmart.entity.UserPurchase;
import uz.drivesmart.entity.UserTestAccess;
import uz.drivesmart.enums.PaymentMethod;
import uz.drivesmart.enums.PaymentStatus;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.TestPackageRepository;
import uz.drivesmart.repository.UserPurchaseRepository;
import uz.drivesmart.repository.UserRepository;
import uz.drivesmart.repository.UserTestAccessRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final UserPurchaseRepository purchaseRepository;
    private final TestPackageRepository packageRepository;
    private final UserRepository userRepository;
    private final UserTestAccessRepository accessRepository;
    private final FileStorageService fileStorageService;
    private final ClickPaymentService clickPaymentService;
    private final PaymePaymentService paymePaymentService;
    private final UzumPaymentService uzumPaymentService;

    /**
     * To'lovni boshlash
     */
    @Transactional
    public PurchaseResponse initiatePurchase(Long userId, PurchaseInitRequest request) {
        log.info("Initiating purchase for user: {}, package: {}, method: {}",
                userId, request.packageId(), request.paymentMethod());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        TestPackage pkg = packageRepository.findByIdAndIsActiveTrue(request.packageId())
                .orElseThrow(() -> new ResourceNotFoundException("Test paketi topilmadi"));

        // Allaqachon sotib olinganini tekshirish
        boolean alreadyPurchased = purchaseRepository.existsByUserIdAndTestPackageIdAndStatus(
                userId, pkg.getId(), PaymentStatus.COMPLETED);

        if (alreadyPurchased) {
            throw new BusinessException("Siz bu test paketini allaqachon sotib olgansiz");
        }

        // Purchase yaratish
        UserPurchase purchase = new UserPurchase();
        purchase.setUser(user);
        purchase.setTestPackage(pkg);
        purchase.setAmount(pkg.getPrice());
        purchase.setPaymentMethod(request.paymentMethod());
        purchase.setStatus(PaymentStatus.PENDING);
        purchase.setPurchasedAt(LocalDateTime.now());
        purchase.setExpiresAt(LocalDateTime.now().plusDays(pkg.getDurationDays()));

        String paymentUrl = null;

        // To'lov usulini qayta ishlash
        switch (request.paymentMethod()) {
            case CLICK -> {
                String transactionId = clickPaymentService.createInvoice(purchase);
                purchase.setTransactionId(transactionId);
                paymentUrl = clickPaymentService.getPaymentUrl(transactionId);
            }
            case PAYME -> {
                String transactionId = paymePaymentService.createInvoice(purchase);
                purchase.setTransactionId(transactionId);
                paymentUrl = paymePaymentService.getPaymentUrl(transactionId);
            }
            case UZUM -> {
                String transactionId = uzumPaymentService.createInvoice(purchase);
                purchase.setTransactionId(transactionId);
                paymentUrl = uzumPaymentService.getPaymentUrl(transactionId);
            }
            case CARD_TRANSFER -> {
                purchase.setTransactionId(UUID.randomUUID().toString());
                purchase.setStatus(PaymentStatus.AWAITING_CONFIRMATION);
            }
        }

        purchase = purchaseRepository.save(purchase);
        log.info("Purchase created with ID: {}", purchase.getId());

        return toPurchaseResponse(purchase, paymentUrl);
    }

    /**
     * Karta o'tkazmasi (check yuklash)
     */
    @Transactional
    public PurchaseResponse submitCardTransfer(Long userId, CardTransferRequest request) {
        log.info("Card transfer submission for user: {}, package: {}", userId, request.packageId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        TestPackage pkg = packageRepository.findByIdAndIsActiveTrue(request.packageId())
                .orElseThrow(() -> new ResourceNotFoundException("Test paketi topilmadi"));

        // Check rasmini saqlash
        String receiptPath = fileStorageService.saveImageFromBase64(
                request.receiptImageBase64(),
                "receipt"
        );

        // Purchase yaratish
        UserPurchase purchase = new UserPurchase();
        purchase.setUser(user);
        purchase.setTestPackage(pkg);
        purchase.setAmount(pkg.getPrice());
        purchase.setPaymentMethod(PaymentMethod.CARD_TRANSFER);
        purchase.setStatus(PaymentStatus.AWAITING_CONFIRMATION);
        purchase.setTransactionId(UUID.randomUUID().toString());
        purchase.setReceiptImagePath(receiptPath);
        purchase.setPurchasedAt(LocalDateTime.now());
        purchase.setExpiresAt(LocalDateTime.now().plusDays(pkg.getDurationDays()));

        purchase = purchaseRepository.save(purchase);
        log.info("Card transfer purchase created with ID: {}", purchase.getId());

        return toPurchaseResponse(purchase, null);
    }

    /**
     * Admin tomonidan tasdiqlash
     */
    @Transactional
    public void confirmCardTransfer(Long purchaseId, String adminNotes) {
        log.info("Admin confirming purchase: {}", purchaseId);

        UserPurchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResourceNotFoundException("To'lov topilmadi"));

        if (purchase.getStatus() != PaymentStatus.AWAITING_CONFIRMATION) {
            throw new BusinessException("Bu to'lov tasdiqlanishi mumkin emas");
        }

        purchase.setStatus(PaymentStatus.COMPLETED);
        purchase.setConfirmedAt(LocalDateTime.now());
        purchase.setAdminNotes(adminNotes);
        purchaseRepository.save(purchase);

        // Access berish
        grantAccess(purchase);

        log.info("Purchase confirmed and access granted");
    }

    /**
     * Webhook'lar uchun to'lovni tasdiqlash
     */
    @Transactional
    public void confirmPayment(String transactionId) {
        log.info("Confirming payment for transaction: {}", transactionId);

        UserPurchase purchase = purchaseRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Tranzaksiya topilmadi"));

        if (purchase.getStatus() == PaymentStatus.COMPLETED) {
            log.warn("Payment already confirmed: {}", transactionId);
            return;
        }

        purchase.setStatus(PaymentStatus.COMPLETED);
        purchase.setConfirmedAt(LocalDateTime.now());
        purchaseRepository.save(purchase);

        // Access berish
        grantAccess(purchase);

        log.info("Payment confirmed and access granted for transaction: {}", transactionId);
    }

    /**
     * Foydalanuvchiga access berish
     */
    private void grantAccess(UserPurchase purchase) {
        TestPackage pkg = purchase.getTestPackage();

        UserTestAccess access = new UserTestAccess();
        access.setUser(purchase.getUser());
        access.setTestPackage(pkg);
        access.setPurchase(purchase);
        access.setRemainingAttempts(pkg.getMaxAttempts());
        access.setAccessedAt(LocalDateTime.now());
        access.setExpiresAt(purchase.getExpiresAt());
        access.setIsActive(true);

        accessRepository.save(access);
        log.info("Access granted to user: {}, package: {}",
                purchase.getUser().getId(), pkg.getId());
    }

    /**
     * Foydalanuvchi xaridlari tarixi
     */
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getUserPurchases(Long userId) {
        log.info("Fetching purchases for user: {}", userId);
        return purchaseRepository.findByUserIdOrderByPurchasedAtDesc(userId)
                .stream()
                .map(p -> toPurchaseResponse(p, null))
                .collect(Collectors.toList());
    }

    /**
     * Admin uchun tasdiqlash kutayotgan to'lovlar
     */
    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPendingCardTransfers() {
        log.info("Fetching pending card transfers");
        return purchaseRepository.findByStatusOrderByPurchasedAtDesc(
                        PaymentStatus.AWAITING_CONFIRMATION)
                .stream()
                .map(p -> toPurchaseResponse(p, null))
                .collect(Collectors.toList());
    }

    private PurchaseResponse toPurchaseResponse(UserPurchase purchase, String paymentUrl) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUser().getId(),
                purchase.getTestPackage().getId(),
                purchase.getTestPackage().getName(),
                purchase.getAmount(),
                purchase.getPaymentMethod(),
                purchase.getStatus(),
                purchase.getTransactionId(),
                purchase.getPurchasedAt(),
                purchase.getExpiresAt(),
                paymentUrl
        );
    }
}