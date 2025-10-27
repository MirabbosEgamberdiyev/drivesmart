package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.AnswerResultResponse;
import uz.drivesmart.dto.response.TestResultResponse;
import uz.drivesmart.dto.response.TestSessionResponse;
import uz.drivesmart.entity.User;
import uz.drivesmart.service.TestService;

import java.util.List;

@Tag(name = "Test", description = "Test sessiyasi API")
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @Operation(summary = "Testni boshlash")
    @PostMapping("/start")
    public ResponseEntity<TestSessionResponse> startTest(
            @Valid @RequestBody TestRequest request,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.startTest(user.getId(), request));
    }

    @Operation(summary = "Javob yuborish")
    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<AnswerResultResponse> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.submitAnswer(user.getId(), sessionId, request));
    }

    @Operation(summary = "Test natijasi")
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<TestResultResponse> getResult(
            @PathVariable Long sessionId,
            Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.getResult(user.getId(), sessionId));
    }

    @Operation(summary = "Test tarixi")
    @GetMapping("/history")
    public ResponseEntity<List<TestSessionResponse>> getHistory(Authentication authentication) {
        var user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(testService.getHistory(user.getId()));
    }
}
