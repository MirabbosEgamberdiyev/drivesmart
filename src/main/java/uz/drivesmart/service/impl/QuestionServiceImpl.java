package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.drivesmart.dto.request.BulkQuestionRequest;
import uz.drivesmart.dto.request.QuestionRequest;
import uz.drivesmart.dto.response.BulkQuestionResponse;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.entity.Question;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.QuestionRepository;
import uz.drivesmart.service.QuestionService;
import uz.drivesmart.util.MapperUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final MapperUtil mapperUtil;

    @Override
    public QuestionResponse addQuestion(QuestionRequest request) {
        var question = new Question();
        question.setText(request.text());
        question.setOptions(request.options());
        question.setCorrectAnswer(request.correctAnswer());
        question.setTopic(request.topic());
        questionRepository.save(question);
        return mapperUtil.toQuestionResponse(question);
    }

    @Override
    public BulkQuestionResponse addBulkQuestions(BulkQuestionRequest request) {
        List<QuestionResponse> successful = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (QuestionRequest req : request.questions()) {
            try {
                var question = new Question();
                question.setText(req.text());
                question.setOptions(req.options());
                question.setCorrectAnswer(req.correctAnswer());
                question.setTopic(req.topic());
                questionRepository.save(question);
                successful.add(mapperUtil.toQuestionResponse(question));
            } catch (Exception e) {
                errors.add("Savol qo'shishda xato: " + req.text() + " - " + e.getMessage());
            }
        }

        return new BulkQuestionResponse(successful, errors);
    }

    @Override
    public List<QuestionResponse> getAllQuestions(String topic) {
        List<Question> questions = topic == null
                ? questionRepository.findAll()
                : questionRepository.findByTopic(topic);
        return questions.stream()
                .map(mapperUtil::toQuestionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        var question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savol topilmadi: " + id));
        question.setText(request.text());
        question.setOptions(request.options());
        question.setCorrectAnswer(request.correctAnswer());
        question.setTopic(request.topic());
        questionRepository.save(question);
        return mapperUtil.toQuestionResponse(question);
    }

    @Override
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Savol topilmadi: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Override
    public List<String> getAllTopics() {
        return questionRepository.findDistinctTopics();
    }
}