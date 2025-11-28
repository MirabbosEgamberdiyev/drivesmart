package uz.drivesmart.dto.response;

import java.util.List;

/**
 * Har bir javob bo'yicha batafsil ma'lumot
 */


public record AnswerDetailDto(
        Long questionId,
        String questionText,
        List<String> options,
        String selectedAnswer,
        String correctAnswer,
        String explanation,
        boolean isCorrect,
        String imageUrl
) {}