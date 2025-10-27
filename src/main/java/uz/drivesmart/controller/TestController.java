package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.*;
import uz.drivesmart.entity.User;
import uz.drivesmart.service.TestService;

import java.util.List;

@Tag(name = "Test", description = "Test sessiyasi API")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
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
            @ApiResponse(responseCode = "404", description = "Foydalanuvchi topilmadi")
    })
    @PostMapping("/start")
    public ResponseEntity<TestStartResponse> startTest(
            @Valid @RequestBody TestRequest request,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.startTest(user.getId(), request));
    }

    @Operation(
            summary = "Barcha javoblarni yuborish)",
            description = "Foydalanuvchi test tugagach barcha javoblarni bir vaqtda yuboradi. " +
                    "Response sifatida batafsil natija qaytariladi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Javoblar qabul qilindi"),
            @ApiResponse(responseCode = "400", description = "Test vaqti tugagan yoki allaqachon tugallangan"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @PostMapping("/submit")
    public ResponseEntity<TestResultDetailedResponse> submitAllAnswers(
            @Valid @RequestBody SubmitAllAnswersRequest request,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.submitAllAnswers(user.getId(), request));
    }

    @Operation(
            summary = "Test natijasini qayta ko'rish",
            description = "Tugallangan testning batafsil natijasini olish (har bir savol bo'yicha tahlil)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Natija topildi"),
            @ApiResponse(responseCode = "400", description = "Test hali tugallanmagan"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<TestResultDetailedResponse> getDetailedResult(
            @PathVariable Long sessionId,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.getResultById(user.getId(), sessionId));
    }

    @Operation(
            summary = "Test tarixini ko'rish",
            description = "Foydalanuvchining barcha testlarini ko'rish"
    )
    @GetMapping("/history")
    public ResponseEntity<List<TestSessionResponse>> getHistory(
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.getHistory(user.getId()));
    }

    @Operation(
            summary = "Testni bekor qilish",
            description = "Test jarayonida foydalanuvchi testni bekor qilishi mumkin"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Test bekor qilindi"),
            @ApiResponse(responseCode = "400", description = "Tugallangan testni bekor qilib bo'lmaydi"),
            @ApiResponse(responseCode = "404", description = "Sessiya topilmadi")
    })
    @DeleteMapping("/{sessionId}/abandon")
    public ResponseEntity<Void> abandonTest(
            @PathVariable Long sessionId,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        testService.abandonTest(user.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "[DEPRECATED] Har bir javobni alohida yuborish",
            description = "Eski usul. submitAllAnswers dan foydalaning!"
    )
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResultResponse> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.submitAnswer(user.getId(), sessionId, request));
    }

    @Operation(
            summary = "[DEPRECATED] Oddiy natija",
            description = "Eski format. getDetailedResult dan foydalaning!"
    )
    @GetMapping("/{sessionId}/result-simple")
    public ResponseEntity<TestResultResponse> getSimpleResult(
            @PathVariable Long sessionId,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.getResult(user.getId(), sessionId));
    }
}