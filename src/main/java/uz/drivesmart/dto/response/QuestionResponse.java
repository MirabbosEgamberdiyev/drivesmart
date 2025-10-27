package uz.drivesmart.dto.response;

import uz.drivesmart.entity.Question;
import uz.drivesmart.service.FileStorageService;

import java.util.List;

public record QuestionResponse(
        Long id,
        String text,
        List<String> options,
        String topic,
        String imageBase64 // Frontend uchun Base64
) {
    // ✅ Static factory method
    public static QuestionResponse from(Question question, FileStorageService fileService) {
        String imageData = question.hasImage()
                ? fileService.getImageAsBase64(question.getImagePath())
                : null;

        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getOptions(),
                question.getTopic(),
                imageData
        );
    }
}