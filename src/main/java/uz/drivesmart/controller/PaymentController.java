package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.CardTransferRequest;
import uz.drivesmart.dto.request.PurchaseInitRequest;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.PurchaseResponse;
import uz.drivesmart.dto.response.TestPackageDto;
import uz.drivesmart.dto.response.UserAccessDto;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.service.PaymentService;
import uz.drivesmart.service.TestAccessService;
import uz.drivesmart.service.TestPackageService;

import java.util.List;

@Tag(name = "Payment", description = "To'lov va test paketlari boshqaruvi")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;
    private final TestPackageService packageService;
    private final TestAccessService accessService;

    // ==================== TEST PACKAGES ====================

    @Operation(
            summary = "Barcha test paketlari",
            description = "Sotuvda mavjud barcha test paketlarini ko'rish"
    )
    @GetMapping("/packages")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<TestPackageDto>>> getAllPackages() {
        List<TestPackageDto> packages = packageService.getAllActivePackages();
        return ResponseEntity.ok(ApiResponseDto.success(packages));
    }

    @Operation(
            summary = "Paket ma'lumotlari",
            description = "Bitta test paketi haqida batafsil ma'lumot"
    )
    @GetMapping("/packages/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<TestPackageDto>> getPackageById(@PathVariable Long id) {
        TestPackageDto pkg = packageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponseDto.success(pkg));
    }

    @Operation(
            summary = "Mavzu bo'yicha paketlar",
            description = "Ma'lum mavzuga oid test paketlari"
    )
    @GetMapping("/packages/topic/{topic}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<TestPackageDto>>> getPackagesByTopic(
            @PathVariable String topic) {
        List<TestPackageDto> packages = packageService.getPackagesByTopic(topic);
        return ResponseEntity.ok(ApiResponseDto.success(packages));
    }

    // ==================== PURCHASE ====================

    @Operation(
            summary = "To'lovni boshlash",
            description = "Test paketini sotib olish uchun to'lovni boshlash. " +
                    "Click/Payme/Uzum uchun to'lov URL qaytariladi"
    )
    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<PurchaseResponse>> initiatePurchase(
            @Valid @RequestBody PurchaseInitRequest request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        PurchaseResponse response = paymentService.initiatePurchase(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("To'lov muvaffaqiyatli boshlandi", response)
        );
    }

    @Operation(
            summary = "Karta o'tkazmasi",
            description = "Karta orqali to'lov amalga oshirilganda check rasmini yuklash. " +
                    "Admin tasdiqlashini kutadi"
    )
    @PostMapping("/purchase/card-transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<PurchaseResponse>> submitCardTransfer(
            @Valid @RequestBody CardTransferRequest request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        PurchaseResponse response = paymentService.submitCardTransfer(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Check yuklandi. Admin tasdiqini kutmoqda", response)
        );
    }

    @Operation(
            summary = "Xaridlar tarixi",
            description = "Foydalanuvchining barcha to'lovlari tarixi"
    )
    @GetMapping("/my-purchases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<PurchaseResponse>>> getMyPurchases(
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        List<PurchaseResponse> purchases = paymentService.getUserPurchases(userId);

        return ResponseEntity.ok(ApiResponseDto.success(purchases));
    }

    // ==================== ACCESS ====================

    @Operation(
            summary = "Mening kirish huquqlarim",
            description = "Sotib olingan va active bo'lgan barcha test paketlari"
    )
    @GetMapping("/my-access")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<UserAccessDto>>> getMyAccess(
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        List<UserAccessDto> access = accessService.getUserActiveAccess(userId);

        return ResponseEntity.ok(ApiResponseDto.success(access));
    }

    @Operation(
            summary = "Kirish huquqini tekshirish",
            description = "Foydalanuvchi ma'lum paketga kirish huquqi borligini tekshirish"
    )
    @GetMapping("/check-access/{packageId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<Boolean>> checkAccess(
            @PathVariable Long packageId,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        boolean hasAccess = accessService.canAccessTest(userId, packageId);

        return ResponseEntity.ok(ApiResponseDto.success(hasAccess));
    }

    // ==================== ADMIN ====================

    @Operation(
            summary = "Tasdiqlash kutayotgan to'lovlar",
            description = "Karta o'tkazmasi orqali amalga oshirilgan va admin tasdiqini " +
                    "kutayotgan to'lovlar ro'yxati (faqat admin)"
    )
    @GetMapping("/admin/pending-transfers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<PurchaseResponse>>> getPendingTransfers() {
        List<PurchaseResponse> pending = paymentService.getPendingCardTransfers();
        return ResponseEntity.ok(ApiResponseDto.success(pending));
    }

    @Operation(
            summary = "To'lovni tasdiqlash",
            description = "Admin tomonidan karta o'tkazmasini tasdiqlash"
    )
    @PostMapping("/admin/confirm/{purchaseId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> confirmCardTransfer(
            @PathVariable Long purchaseId,
            @RequestParam(required = false) String notes) {

        paymentService.confirmCardTransfer(purchaseId, notes);

        return ResponseEntity.ok(
                ApiResponseDto.success("To'lov tasdiqlandi va kirish huquqi berildi", null)
        );
    }
}