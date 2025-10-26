package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.*;
import uz.drivesmart.entity.TestSession;
import uz.drivesmart.entity.UserAnswer;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.*;
import uz.drivesmart.service.TestService;
import uz.drivesmart.util.MapperUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserAnswerRepository answerRepository;
    private final MapperUtil mapperUtil;

    @Override
    @Transactional
    public TestSessionResponse startTest(Long userId, TestRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        var questions = questionRepository.findRandomByTopic(
                request.topic(),
                request.questionCount()
        );

        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("Savollar topilmadi");
        }

        var session = new TestSession();
        session.setUser(user);
        session.setTopic(request.topic());
        session.setTotalQuestions(questions.size());
        session.setStartedAt(LocalDateTime.now());

        return mapperUtil.toTestSessionResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public AnswerResultResponse submitAnswer(Long userId, Long sessionId, SubmitAnswerRequest request) {
        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        var question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Savol topilmadi"));

        boolean isCorrect = question.getCorrectAnswer().equals(request.answer());

        var answer = new UserAnswer();
        answer.setUser(session.getUser());
        answer.setQuestion(question);
        answer.setTestSession(session);
        answer.setSelectedAnswer(request.answer());
        answer.setCorrect(isCorrect);
        answer.setAnsweredAt(LocalDateTime.now());
        answerRepository.save(answer);

        if (isCorrect) {
            session.setScore(session.getScore() + 1);
            sessionRepository.save(session);
        }

        return new AnswerResultResponse(isCorrect, question.getCorrectAnswer(), session.getScore());
    }

    @Override
    public TestResultResponse getResult(Long userId, Long sessionId) {
        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        var answers = answerRepository.findByTestSessionId(sessionId);
        long correct = answers.stream().filter(UserAnswer::isCorrect).count();
        double percentage = (correct * 100.0) / session.getTotalQuestions();

        return new TestResultResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                (int) correct,
                session.getScore(),
                percentage,
                session.getStartedAt(),
                LocalDateTime.now()
        );
    }

    @Override
    public List<TestSessionResponse> getHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(mapperUtil::toTestSessionResponse)
                .toList();
    }
}