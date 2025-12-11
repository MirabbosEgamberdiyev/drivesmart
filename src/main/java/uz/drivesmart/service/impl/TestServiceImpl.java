package uz.drivesmart.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.request.*;
import uz.drivesmart.dto.response.*;
import uz.drivesmart.entity.Question;
import uz.drivesmart.entity.TestSession;
import uz.drivesmart.entity.UserAnswer;
import uz.drivesmart.enums.TestStatus;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.*;
import uz.drivesmart.service.TestAccessService;
import uz.drivesmart.service.TestService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final QuestionRepository questionRepository;
    private final TestSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final UserAnswerRepository answerRepository;
    private final TestPackageRepository packageRepository;
    private final TestAccessService accessService;

    private static final double PASS_THRESHOLD = 70.0;

    /**
     * ✅ Testni boshlash - PULLIK PAKETLAR UCHUN ACCESS CONTROL
     */
    @Override
    @Transactional
    public TestStartResponse startTest(Long userId, TestRequest request) {
        log.info("Test boshlandi: userId={}, topic={}, count={}",
                userId, request.topic(), request.questionCount());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

        //Test uchun hozircha commentda turadi
        // ✅ Paket topish
//        var testPackage = packageRepository.findByTopicAndActive(request.topic())
//                .stream()
//                .filter(pkg -> pkg.getQuestionCount().equals(request.questionCount()))
//                .findFirst()
//                .orElseThrow(() -> new BusinessException("Bu test paketi topilmadi"));

        // ✅ Kirish huquqini tekshirish
//        if (!accessService.canAccessTest(userId, testPackage.getId())) {
//            throw new BusinessException(
//                    "Bu test paketiga kirish huquqingiz yo'q. Iltimos, avval sotib oling"
//            );
//        }

        // ✅ Urinishni kamaytirish
//        accessService.consumeAttempt(userId, testPackage.getId());

        // ✅ Random savollarni olish
        var questions = questionRepository.findRandomByTopic(
                request.topic(),
                request.questionCount()
        );

        if (questions.size() < request.questionCount()) {
            throw new BusinessException(
                    String.format("Mavzuda faqat %d ta savol mavjud, %d ta so'raldingiz",
                            questions.size(), request.questionCount())
            );
        }

        // ✅ Test session yaratish
        var session = new TestSession();
        session.setUser(user);
        session.setTopic(request.topic());
        session.setTotalQuestions(questions.size());
        session.setStartedAt(LocalDateTime.now());
        session.setDurationMinutes(30);
        session.setStatus(TestStatus.IN_PROGRESS);
        session = sessionRepository.save(session);

        // ✅ Frontend uchun savollar (correctAnswer va explanation BILAN)
        List<QuestionForTestDto> questionDtos = questions.stream()
                .map(q -> new QuestionForTestDto(
                        q.getId(),
                        q.getText(),
                        q.getOptions(),
                        getImageUrl(q.getImagePath()),
                        q.getCorrectAnswer(),     // ✅ Frontend'da tekshirish uchun
                        q.getExplanation()        // ✅ Frontend'da ko'rsatish uchun
                ))
                .toList();

        LocalDateTime expiresAt = session.getStartedAt()
                .plusMinutes(session.getDurationMinutes());

        log.info("Test yaratildi: sessionId={}, questions={}", session.getId(), questions.size());

        return new TestStartResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                session.getDurationMinutes(),
                session.getStartedAt(),
                expiresAt,
                questionDtos
        );
    }

    /**
     * ✅ Statistika uchun javoblarni saqlash
     */
    @Override
    @Transactional
    public TestResultDetailedResponse submitAllAnswers(
            Long userId,
            SubmitAllAnswersRequest request
    ) {
        log.info("Barcha javoblar yuborildi: userId={}, sessionId={}, answers={}",
                userId, request.sessionId(), request.answers().size());

        var session = sessionRepository.findByIdAndUserId(request.sessionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        if (session.getStatus() == TestStatus.COMPLETED) {
            log.warn("Test allaqachon tugallangan: sessionId={}", request.sessionId());
            return getResultById(userId, request.sessionId());
        }

        if (session.getStatus() == TestStatus.ABANDONED) {
            throw new BusinessException("Test bekor qilingan");
        }

        if (session.isExpired()) {
            log.warn("Test vaqti tugadi: sessionId={}", request.sessionId());
            session.setStatus(TestStatus.ABANDONED);
            session.setFinishedAt(LocalDateTime.now());
            sessionRepository.save(session);
            throw new BusinessException("Test vaqti tugadi");
        }

        boolean hasAnswers = answerRepository.existsByTestSessionId(request.sessionId());
        if (hasAnswers) {
            throw new BusinessException("Bu testga javoblar allaqachon yuborilgan");
        }

        List<Long> questionIds = request.answers().stream()
                .map(UserAnswerDto::questionId)
                .toList();

        Map<Long, Question> questionsMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<UserAnswer> userAnswers = new ArrayList<>();
        int correctCount = 0;

        for (var answerDto : request.answers()) {
            Question question = questionsMap.get(answerDto.questionId());
            if (question == null) {
                log.error("Savol topilmadi: questionId={}", answerDto.questionId());
                throw new ResourceNotFoundException("Savol topilmadi: " + answerDto.questionId());
            }

            boolean isCorrect = question.getCorrectAnswer()
                    .trim()
                    .equalsIgnoreCase(answerDto.selectedAnswer().trim());

            if (isCorrect) {
                correctCount++;
            }

            var userAnswer = new UserAnswer();
            userAnswer.setUser(session.getUser());
            userAnswer.setQuestion(question);
            userAnswer.setTestSession(session);
            userAnswer.setSelectedAnswer(answerDto.selectedAnswer());
            userAnswer.setCorrect(isCorrect);
            userAnswer.setAnsweredAt(LocalDateTime.now());
            userAnswers.add(userAnswer);
        }

        // ✅ Statistika uchun saqlash
        answerRepository.saveAll(userAnswers);

        int wrongCount = session.getTotalQuestions() - correctCount;
        session.setScore(correctCount);
        session.setCorrectCount(correctCount);
        session.setWrongCount(wrongCount);
        session.setStatus(TestStatus.COMPLETED);
        session.setFinishedAt(LocalDateTime.now());
        sessionRepository.save(session);

        log.info("Test tugallandi: sessionId={}, correct={}, wrong={}",
                session.getId(), correctCount, wrongCount);

        return buildDetailedResult(session, userAnswers, questionsMap);
    }

    @Override
    @Transactional(readOnly = true)
    public TestResultDetailedResponse getResultById(Long userId, Long sessionId) {
        log.info("Natija so'raldi: userId={}, sessionId={}", userId, sessionId);

        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        if (session.getStatus() != TestStatus.COMPLETED) {
            throw new BusinessException("Test hali tugallanmagan");
        }

        List<UserAnswer> userAnswers = answerRepository.findByTestSessionId(sessionId);

        List<Long> questionIds = userAnswers.stream()
                .map(ua -> ua.getQuestion().getId())
                .toList();

        Map<Long, Question> questionsMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        return buildDetailedResult(session, userAnswers, questionsMap);
    }

    private TestResultDetailedResponse buildDetailedResult(
            TestSession session,
            List<UserAnswer> userAnswers,
            Map<Long, Question> questionsMap
    ) {
        List<AnswerDetailDto> answerDetails = userAnswers.stream()
                .map(ua -> {
                    Question q = ua.getQuestion();
                    if (questionsMap.containsKey(q.getId())) {
                        q = questionsMap.get(q.getId());
                    }
                    return new AnswerDetailDto(
                            q.getId(),
                            q.getText(),
                            q.getOptions(),
                            ua.getSelectedAnswer(),
                            q.getCorrectAnswer(),
                            q.getExplanation(),
                            ua.isCorrect(),
                            getImageUrl(q.getImagePath())
                    );
                })
                .toList();

        double percentage = (session.getCorrectCount() * 100.0) / session.getTotalQuestions();
        boolean passed = percentage >= PASS_THRESHOLD;

        long durationSeconds = Duration.between(
                session.getStartedAt(),
                session.getFinishedAt()
        ).getSeconds();

        return new TestResultDetailedResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                session.getCorrectCount(),
                session.getWrongCount(),
                session.getScore(),
                percentage,
                passed,
                session.getStartedAt(),
                session.getFinishedAt(),
                durationSeconds,
                answerDetails
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestSessionResponse> getHistory(Long userId) {
        log.info("Test tarixi so'raldi: userId={}", userId);

        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toTestSessionResponse)
                .toList();
    }

    @Override
    @Transactional
    public void abandonTest(Long userId, Long sessionId) {
        log.info("Test bekor qilindi: userId={}, sessionId={}", userId, sessionId);

        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        if (session.getStatus() == TestStatus.COMPLETED) {
            throw new BusinessException("Tugallangan testni bekor qilib bo'lmaydi");
        }

        session.setStatus(TestStatus.ABANDONED);
        session.setFinishedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    private String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        return "/api/images/" + imagePath;
    }

    private TestSessionResponse toTestSessionResponse(TestSession session) {
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