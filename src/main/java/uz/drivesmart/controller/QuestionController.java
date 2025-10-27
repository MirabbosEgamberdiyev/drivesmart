package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.BulkQuestionRequest;
import uz.drivesmart.dto.request.QuestionRequest;
import uz.drivesmart.dto.response.BulkQuestionResponse;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.service.QuestionService;

import java.util.List;

@Tag(name = "Question", description = "Savollar boshqaruvi API (Admin uchun)")
@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @Operation(
            summary = "Yangi savol qo'shish",
            description = "Bitta savol qo'shadi"
    )
    @ApiResponse(responseCode = "200", description = "Savol muvaffaqiyatli qo'shildi")
    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestion(
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.addQuestion(request));
    }

    @Operation(
            summary = "Ko'plab savollarni qo'shish",
            description = "Bir nechta savollarni bir vaqtning o'zida qo'shadi. " +
                    "Xatolik bo'lsa ham qolgan savollar qo'shiladi."
    )
    @ApiResponse(responseCode = "200", description = "Bulk import yakunlandi")
    @PostMapping("/bulk")
    public ResponseEntity<BulkQuestionResponse> addBulkQuestions(
            @Valid @RequestBody BulkQuestionRequest request) {
        return ResponseEntity.ok(questionService.addBulkQuestions(request));
    }

    @Operation(summary = "Barcha savollar")
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions(
            @RequestParam(required = false) String topic) {
        return ResponseEntity.ok(questionService.getAllQuestions(topic));
    }

    @Operation(summary = "Savolni yangilash")
    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @Operation(summary = "Savolni o'chirish")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mavzular ro'yxati")
    @GetMapping("/topics")
    public ResponseEntity<List<String>> getAllTopics() {
        return ResponseEntity.ok(questionService.getAllTopics());
    }
}
