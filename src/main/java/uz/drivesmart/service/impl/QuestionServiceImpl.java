package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.request.BulkQuestionRequest;
import uz.drivesmart.dto.request.QuestionRequest;
import uz.drivesmart.dto.response.BulkQuestionResponse;
import uz.drivesmart.dto.response.QuestionResponse;
import uz.drivesmart.entity.Question;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.QuestionRepository;
import uz.drivesmart.service.FileStorageService;
import uz.drivesmart.service.QuestionService;
import uz.drivesmart.util.MapperUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionRepository questionRepository;
    private final FileStorageService fileStorageService;
    private final MapperUtil mapperUtil;

    @Override
    @Transactional
    public QuestionResponse addQuestion(QuestionRequest request) {
        var question = new Question();
        question.setText(request.text());
        question.setOptions(request.options());
        question.setCorrectAnswer(request.correctAnswer());
        question.setTopic(request.topic());

        // ✅ Base64'dan file'ga convert
        if (request.imageBase64() != null && !request.imageBase64().isBlank()) {
            String filename = fileStorageService.saveImageFromBase64(
                    request.imageBase64(),
                    "question"
            );
            question.setImagePath(filename);
        }

        questionRepository.save(question);
        return mapperUtil.toQuestionResponse(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        var question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savol topilmadi"));

        // ✅ File'ni ham o'chirish
        if (question.hasImage()) {
            fileStorageService.deleteImage(question.getImagePath());
        }

        questionRepository.deleteById(id);
    }


    @Override
    @Transactional
    public BulkQuestionResponse addBulkQuestions(BulkQuestionRequest request) {
        List<QuestionResponse> successful = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (QuestionRequest req : request.questions()) {
            try {
                validateQuestionRequest(req); // ✅ Extra validation

                var question = new Question();
                question.setText(req.text());
                question.setOptions(req.options());
                question.setCorrectAnswer(req.correctAnswer());
                question.setTopic(req.topic());

                if (req.imageBase64() != null && !req.imageBase64().isBlank()) {
                    String filename = fileStorageService.saveImageFromBase64(
                            req.imageBase64(),
                            "question"
                    );
                    question.setImagePath(filename);
                }

                questionRepository.save(question);
                successful.add(mapperUtil.toQuestionResponse(question));

            } catch (Exception e) {
                errors.add("Savol qo'shishda xato: " + req.text() + " - " + e.getMessage());
            }
        }

        // ✅ Batch save (optimization)
        if (!successful.isEmpty()) {
            questionRepository.flush();
        }

        return new BulkQuestionResponse(successful, errors);
    }

    private void validateQuestionRequest(QuestionRequest request) {
        if (!request.options().contains(request.correctAnswer())) {
            throw new BusinessException("To'g'ri javob variantlar ichida bo'lishi kerak");
        }

        // ✅ Check duplicates
        boolean exists = questionRepository.existsByTextAndTopic(
                request.text(),
                request.topic()
        );
        if (exists) {
            throw new BusinessException("Bunday savol allaqachon mavjud");
        }
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
    public List<String> getAllTopics() {
        return questionRepository.findDistinctTopics();
    }
}