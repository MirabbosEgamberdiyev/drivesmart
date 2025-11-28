package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.DashboardStatsDto;
import uz.drivesmart.service.AdminDashboardService;

@Tag(name = "Admin Dashboard", description = "Admin panel statistikasi")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @Operation(
            summary = "Dashboard statistikasi",
            description = "Umumiy tizim statistikasi (foydalanuvchilar, testlar, savollar)"
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto response = dashboardService.getDashboardStats();

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "So'nggi testlar",
            description = "Oxirgi 10 ta tugallangan test"
    )
    @GetMapping("/recent-tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> getRecentTests(
            @RequestParam(defaultValue = "10") int limit) {

        var response = dashboardService.getRecentTests(limit);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Mavzu bo'yicha statistika",
            description = "Har bir mavzu bo'yicha testlar soni va o'rtacha natija"
    )
    @GetMapping("/topic-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<?>> getTopicStats() {
        var response = dashboardService.getTopicStats();

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }
}