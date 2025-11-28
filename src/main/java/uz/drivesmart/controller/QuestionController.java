package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.BulkQuestionRequest;
import uz.drivesmart.dto.request.QuestionRequest;
import uz.drivesmart.dto.response.ApiResponseDto;
import uz.drivesmart.dto.response.BulkQuestionResponse;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.service.QuestionService;

import java.util.List;

@Tag(name = "Question", description = "Savollar boshqaruvi API (Admin uchun)")
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "Yangi savol qo'shish",
            description = "Bitta savol qo'shadi. Rasm Base64 formatda yuboriladi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Savol muvaffaqiyatli qo'shildi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumotlar"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<QuestionResponse>> addQuestion(
            @Valid @RequestBody QuestionRequest request) {

        QuestionResponse response = questionService.addQuestion(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Savol muvaffaqiyatli qo'shildi", response));
    }

    @Operation(
            summary = "Ko'plab savollarni qo'shish",
            description = "Bir nechta savollarni bir vaqtning o'zida qo'shadi. " +
                    "Xatolik bo'lsa ham qolgan savollar qo'shiladi."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk import yakunlandi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar")
    })
    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<BulkQuestionResponse>> addBulkQuestions(
            @Valid @RequestBody BulkQuestionRequest request) {

        BulkQuestionResponse response = questionService.addBulkQuestions(request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Bulk import yakunlandi", response)
        );
    }

    @Operation(
            summary = "Barcha savollar",
            description = "Barcha savollarni yoki mavzu bo'yicha filterlangan savollarni qaytaradi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Savollar topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<QuestionResponse>>> getAllQuestions(
            @RequestParam(required = false) String topic) {

        List<QuestionResponse> response = questionService.getAllQuestions(topic);

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }

    @Operation(
            summary = "Savolni yangilash",
            description = "Mavjud savolni tahrirlash"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Savol yangilandi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri ma'lumotlar"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar"),
            @ApiResponse(responseCode = "404", description = "Savol topilmadi")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {

        QuestionResponse response = questionService.updateQuestion(id, request);

        return ResponseEntity.ok(
                ApiResponseDto.success("Savol muvaffaqiyatli yangilandi", response)
        );
    }

    @Operation(
            summary = "Savolni o'chirish",
            description = "Savolni butunlay o'chirish"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Savol o'chirildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q - faqat adminlar"),
            @ApiResponse(responseCode = "404", description = "Savol topilmadi")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);

        return ResponseEntity.ok(
                ApiResponseDto.success("Savol muvaffaqiyatli o'chirildi", null)
        );
    }

    @Operation(
            summary = "Mavzular ro'yxati",
            description = "Barcha mavjud mavzular ro'yxatini qaytaradi"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mavzular topildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya talab etiladi")
    })
    @GetMapping("/topics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')")
    public ResponseEntity<ApiResponseDto<List<String>>> getAllTopics() {
        List<String> response = questionService.getAllTopics();

        return ResponseEntity.ok(
                ApiResponseDto.success(response)
        );
    }
}