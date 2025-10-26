package uz.drivesmart.service;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.*;
import java.util.List;

public interface TestService {
    TestSessionResponse startTest(Long userId, TestRequest request);
    AnswerResultResponse submitAnswer(Long userId, Long sessionId, SubmitAnswerRequest request);
    TestResultResponse getResult(Long userId, Long sessionId);
    List<TestSessionResponse> getHistory(Long userId);
}