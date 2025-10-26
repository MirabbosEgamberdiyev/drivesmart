package uz.drivesmart.util;

import org.springframework.stereotype.Component;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.dto.response.TestSessionResponse;
import uz.drivesmart.entity.Question;
import uz.drivesmart.entity.TestSession;

@Component
public class MapperUtil {
    public TestSessionResponse toTestSessionResponse(TestSession session) {
        return new TestSessionResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                session.getScore()
        );
    }

    public QuestionResponse toQuestionResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getOptions(),
                question.getTopic()
        );
    }
}