package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.SubmitAllAnswersRequest;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.*;
import uz.drivesmart.security.UserPrincipal;
import uz.drivesmart.service.TestService;

import java.util.List;

@Tag(name = "Test", description = "Test sessiyasi API")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class TestController {

    private final TestService testService;

    @Operation(
            summary = "Testni boshlash",
            description = "Test boshlanganda barcha savollar to'g'ri javoblar va izohlar bilan qaytariladi. " +
                    "Frontend bu ma'lumotlarni yashirin holda saqlaydi va test tugagach ko'rsatadi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test muvaffaqiyatli boshlandi"),
            @ApiResponse(responseCode = "400", description = "Mavzuda yetarli savol yo'q"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "404", description = "Foydalanuvchi topilmadi")
    })
    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<TestStartResponse>> startTest(
            @Valid @RequestBody TestRequest request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        TestStartResponse response = testService.startTest(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Test muvaffaqiyatli boshlandi", response)
        );
    }

    @Operation(
            summary = "Barcha javoblarni yuborish",
            description = "Foydalanuvchi test tugagach barcha javoblarni bir vaqtda yuboradi. " +
                    "Response sifatida batafsil natija qaytariladi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Javoblar qabul qilindi"),
            @ApiResponse(responseCode = "400", description = "Test vaqti tugagan yoki allaqachon tugallangan"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<TestResultDetailedResponse>> submitAllAnswers(
            @Valid @RequestBody SubmitAllAnswersRequest request,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        TestResultDetailedResponse response = testService.submitAllAnswers(userId, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Javoblar muvaffaqiyatli qabul qilindi", response)
        );
    }

    @Operation(
            summary = "Test natijasini qayta ko'rish",
            description = "Tugallangan testning batafsil natijasini olish (har bir savol bo'yicha tahlil)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Natija topildi"),
            @ApiResponse(responseCode = "400", description = "Test hali tugallanmagan"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @GetMapping("/{sessionId}/result")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<TestResultDetailedResponse>> getDetailedResult(
            @PathVariable Long sessionId,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        TestResultDetailedResponse response = testService.getResultById(userId, sessionId);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Test tarixini ko'rish",
            description = "Foydalanuvchining barcha testlarini ko'rish"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarix topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi")
    })
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<TestSessionResponse>>> getHistory(
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        List<TestSessionResponse> response = testService.getHistory(userId);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Testni bekor qilish",
            description = "Test jarayonida foydalanuvchi testni bekor qilishi mumkin"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Test bekor qilindi"),
            @ApiResponse(responseCode = "400", description = "Tugallangan testni bekor qilib bo'lmaydi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @DeleteMapping("/{sessionId}/abandon")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<Void>> abandonTest(
            @PathVariable Long sessionId,
            Authentication authentication) {

        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        testService.abandonTest(userId, sessionId);

        return ResponseEntity.ok(
                ApiResponseDto.success("Test bekor qilindi", null)
        );
    }

//    // ========== DEPRECATED METHODS ==========
//
//    @Operation(
//            summary = "[DEPRECATED] Har bir javobni alohida yuborish",
//            description = "Eski usul. submitAllAnswers dan foydalaning!",
//            deprecated = true
//    )
//    @PostMapping("/{sessionId}/answer")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
//    @Deprecated
//    public ResponseEntity<ApiResponseDto<AnswerResultResponse>> submitAnswer(
//            @PathVariable Long sessionId,
//            @Valid @RequestBody SubmitAnswerRequest request,
//            Authentication authentication) {
//
//        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
//        AnswerResultResponse response = testService.submitAnswer(userId, sessionId, request);
//
//        return ResponseEntity.ok(
//                ApiResponseDto.success(response)
//        );
//    }
//
//    @Operation(
//            summary = "[DEPRECATED] Oddiy natija",
//            description = "Eski format. getDetailedResult dan foydalaning!",
//            deprecated = true
//    )
//    @GetMapping("/{sessionId}/result-simple")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
//    @Deprecated
//    public ResponseEntity<ApiResponseDto<TestResultResponse>> getSimpleResult(
//            @PathVariable Long sessionId,
//            Authentication authentication) {
//
//        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
//        TestResultResponse response = testService.getResult(userId, sessionId);
//
//        return ResponseEntity.ok(
//                ApiResponseDto.success(response)
//        );
//    }
}