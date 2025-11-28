package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.QuestionStatsDto;
import uz.drivesmart.dto.response.UserProgressDto;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.service.StatisticsService;

import java.util.List;

@Tag(name = "Statistics", description = "Foydalanuvchi statistikasi va progress API")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(
            summary = "Mening progressim",
            description = "Joriy foydalanuvchining barcha mavzular bo'yicha progressi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi")
    })
    @GetMapping("/my-progress")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<UserProgressDto>>> getMyProgress(
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        List<UserProgressDto> response = statisticsService.getUserProgress(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Leaderboard - Top performerlar",
            description = "Mavzu bo'yicha eng yaxshi natijaga ega foydalanuvchilar"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leaderboard topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi")
    })
    @GetMapping("/leaderboard/{topic}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<UserProgressDto>>> getLeaderboard(
            @PathVariable String topic,
            @RequestParam(defaultValue = "10") int limit) {

        List<UserProgressDto> response = statisticsService.getTopPerformers(topic, limit);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Eng qiyin savollar",
            description = "Mavzu bo'yicha eng past success rate'ga ega savollar (Admin uchun)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Savollar topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar")
    })
    @GetMapping("/difficult-questions/{topic}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<QuestionStatsDto>>> getDifficultQuestions(
            @PathVariable String topic) {

        List<QuestionStatsDto> response = statisticsService.getDifficultQuestions(topic);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Foydalanuvchi progressi (Admin)",
            description = "Istalgan foydalanuvchining progressini ko'rish"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar")
    })
    @GetMapping("/user/{userId}/progress")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<UserProgressDto>>> getUserProgressById(
            @PathVariable Long userId) {

        List<UserProgressDto> response = statisticsService.getUserProgress(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }
}