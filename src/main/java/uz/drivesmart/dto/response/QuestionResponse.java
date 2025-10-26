package uz.drivesmart.dto.response;

public record QuestionResponse(
        Long id,
        String text,
        java.util.List<String> options,
        String topic
) {}