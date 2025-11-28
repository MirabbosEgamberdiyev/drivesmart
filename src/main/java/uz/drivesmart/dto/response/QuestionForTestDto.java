package uz.drivesmart.dto.response;

import java.util.List;

/**
 * Test uchun savol DTO
 */
public record QuestionForTestDto(
        Long id,
        String text,
        List<String> options,
        String imageUrl,
        String correctAnswer,
        String explanation
) {}