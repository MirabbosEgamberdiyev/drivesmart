package uz.drivesmart.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.dto.response.TestSessionResponse;
import uz.drivesmart.entity.Question;
import uz.drivesmart.entity.TestSession;
import uz.drivesmart.service.FileStorageService;

// ✅ To'liq MapperUtil yarating
@Component
@RequiredArgsConstructor
public class MapperUtil {
    private final FileStorageService fileStorageService;

    public QuestionResponse toQuestionResponse(Question question) {
        String imageData = null;
        if (question.hasImage()) {
            try {
                imageData = fileStorageService.getImageAsBase64(question.getImagePath());
            } catch (Exception e) {
            }
        }

        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getOptions(),
                question.getTopic(),
                imageData
        );
    }

    public TestSessionResponse toTestSessionResponse(TestSession session) {
        return new TestSessionResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                session.getScore(),
                session.getCorrectCount(),
                session.getWrongCount(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getFinishedAt()
        );
    }
}