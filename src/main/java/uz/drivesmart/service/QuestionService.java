package uz.drivesmart.service;

import uz.drivesmart.dto.request.BulkQuestionRequest;
import uz.drivesmart.dto.request.QuestionRequest;
import uz.drivesmart.dto.response.BulkQuestionResponse;
import uz.drivesmart.dto.response.QuestionResponse;
import java.util.List;

public interface QuestionService {
    QuestionResponse addQuestion(QuestionRequest request);
    BulkQuestionResponse addBulkQuestions(BulkQuestionRequest request);
    List<QuestionResponse> getAllQuestions(String topic);
    QuestionResponse updateQuestion(Long id, QuestionRequest request);
    void deleteQuestion(Long id);
    List<String> getAllTopics();
}