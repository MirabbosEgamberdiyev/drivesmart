package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.TestPackageRequest;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.TestPackageDto;
import uz.drivesmart.service.AdminPackageService;

import java.util.List;

@Tag(name = "Admin Package Management", description = "Test paketlarini boshqarish (Admin)")
@RestController
@RequestMapping("/api/admin/packages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminPackageController {

    private final AdminPackageService packageService;

    @Operation(
            summary = "Barcha paketlar",
            description = "Barcha test paketlari (active va inactive)"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<TestPackageDto>>> getAllPackages() {
        List<TestPackageDto> packages = packageService.getAllPackages();
        return ResponseEntity.ok(ApiResponseDto.success(packages));
    }

    @Operation(
            summary = "Yangi paket yaratish",
            description = "Yangi test paketi qo'shish"
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<TestPackageDto>> createPackage(
            @Valid @RequestBody TestPackageRequest request) {

        TestPackageDto pkg = packageService.createPackage(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Paket muvaffaqiyatli yaratildi", pkg));
    }

    @Operation(
            summary = "Paketni yangilash",
            description = "Mavjud paketni tahrirlash"
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<TestPackageDto>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody TestPackageRequest request) {

        TestPackageDto pkg = packageService.updatePackage(id, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Paket muvaffaqiyatli yangilandi", pkg)
        );
    }

    @Operation(
            summary = "Paketni faollashtirish/o'chirish",
            description = "Paket statusini o'zgartirish"
    )
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> togglePackageStatus(@PathVariable Long id) {
        packageService.togglePackageStatus(id);

        return ResponseEntity.ok(
                ApiResponseDto.success("Paket statusi o'zgartirildi", null)
        );
    }

    @Operation(
            summary = "Paketni o'chirish",
            description = "Paketni butunlay o'chirish (faqat SUPER_ADMIN)"
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);

        return ResponseEntity.ok(
                ApiResponseDto.success("Paket muvaffaqiyatli o'chirildi", null)
        );
    }
}
