package uz.drivesmart.dto.response;


import uz.drivesmart.dto.request.FailedQuestion;

import java.util.List;

public record BulkQuestionResponse(
        int total,
        int success,
        int failed,
        List<QuestionResponse> successfulQuestions,
        List<FailedQuestion> failedQuestions
) {

    public BulkQuestionResponse(List<QuestionResponse> successful, List<String> errors) {
        this(
                successful.size() + errors.size(),
                successful.size(),
                errors.size(),
                successful,
                errors.stream()
                        .map(error -> new FailedQuestion(error))
                        .toList()
        );
    }
}