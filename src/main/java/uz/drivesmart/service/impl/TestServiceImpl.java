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

    private static final double PASS_THRESHOLD = 70.0; // 70% dan yuqori - o'tgan

    /**
     * ✅ Testni boshlash - BARCHA savollarni qaytarish
     * ptest.uz formatiga to'liq mos
     */
    @Override
    @Transactional
    public TestStartResponse startTest(Long userId, TestRequest request) {
        log.info("Test boshlandi: userId={}, topic={}, count={}",
                userId, request.topic(), request.questionCount());

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Foydalanuvchi topilmadi"));

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
        session.setDurationMinutes(30); // Default 30 daqiqa
        session.setStatus(TestStatus.IN_PROGRESS);
        session = sessionRepository.save(session);

        // ✅ Savollarni DTO ga o'girish
        List<QuestionForTestDto> questionDtos = questions.stream()
                .map(q -> new QuestionForTestDto(
                        q.getId(),
                        q.getText(),
                        q.getOptions(),
                        getImageUrl(q.getImagePath()),
                        q.getCorrectAnswer(),     // Frontend'da yashiriladi
                        q.getExplanation()        // Test tugagach ko'rsatiladi
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
     * ✅ Barcha javoblarni bir vaqtda qabul qilish va natijani qaytarish
     */
    @Override
    @Transactional
    public TestResultDetailedResponse submitAllAnswers(
            Long userId,
            SubmitAllAnswersRequest request
    ) {
        log.info("Barcha javoblar yuborildi: userId={}, sessionId={}, answers={}",
                userId, request.sessionId(), request.answers().size());

        // ✅ Session'ni tekshirish
        var session = sessionRepository.findByIdAndUserId(request.sessionId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        // ✅ Session statusini tekshirish
        if (session.getStatus() == TestStatus.COMPLETED) {
            log.warn("Test allaqachon tugallangan: sessionId={}", request.sessionId());
            // Allaqachon tugagan bo'lsa, mavjud natijani qaytarish
            return getResultById(userId, request.sessionId());
        }

        if (session.getStatus() == TestStatus.ABANDONED) {
            throw new BusinessException("Test bekor qilingan");
        }

        // ✅ Vaqtni tekshirish
        if (session.isExpired()) {
            log.warn("Test vaqti tugadi: sessionId={}", request.sessionId());
            session.setStatus(TestStatus.ABANDONED);
            session.setFinishedAt(LocalDateTime.now());
            sessionRepository.save(session);
            throw new BusinessException("Test vaqti tugadi");
        }

        // ✅ Takroriy javoblarni tekshirish
        boolean hasAnswers = answerRepository.existsByTestSessionId(request.sessionId());
        if (hasAnswers) {
            throw new BusinessException("Bu testga javoblar allaqachon yuborilgan");
        }

        // ✅ Barcha savollarni bir martalik olish (N+1 muammosini hal qilish)
        List<Long> questionIds = request.answers().stream()
                .map(UserAnswerDto::questionId)
                .toList();

        Map<Long, Question> questionsMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // ✅ Javoblarni tekshirish va saqlash
        List<UserAnswer> userAnswers = new ArrayList<>();
        int correctCount = 0;

        for (var answerDto : request.answers()) {
            Question question = questionsMap.get(answerDto.questionId());
            if (question == null) {
                log.error("Savol topilmadi: questionId={}", answerDto.questionId());
                throw new ResourceNotFoundException("Savol topilmadi: " + answerDto.questionId());
            }

            // ✅ Javobni tekshirish (case-insensitive va whitespace'siz)
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

        // ✅ Barcha javoblarni batch save (performance optimization)
        answerRepository.saveAll(userAnswers);

        // ✅ Session'ni yangilash
        int wrongCount = session.getTotalQuestions() - correctCount;
        session.setScore(correctCount);
        session.setCorrectCount(correctCount);
        session.setWrongCount(wrongCount);
        session.setStatus(TestStatus.COMPLETED);
        session.setFinishedAt(LocalDateTime.now());
        sessionRepository.save(session);

        log.info("Test tugallandi: sessionId={}, correct={}, wrong={}",
                session.getId(), correctCount, wrongCount);

        // ✅ Batafsil natijani tayyorlash
        return buildDetailedResult(session, userAnswers, questionsMap);
    }

    /**
     * ✅ Test natijasini ID bo'yicha olish
     */
    @Override
    @Transactional(readOnly = true)
    public TestResultDetailedResponse getResultById(Long userId, Long sessionId) {
        log.info("Natija so'raldi: userId={}, sessionId={}", userId, sessionId);

        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        if (session.getStatus() != TestStatus.COMPLETED) {
            throw new BusinessException("Test hali tugallanmagan");
        }

        // ✅ Barcha javoblarni olish
        List<UserAnswer> userAnswers = answerRepository.findByTestSessionId(sessionId);

        // ✅ Savollarni olish
        List<Long> questionIds = userAnswers.stream()
                .map(ua -> ua.getQuestion().getId())
                .toList();

        Map<Long, Question> questionsMap = questionRepository.findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        return buildDetailedResult(session, userAnswers, questionsMap);
    }

    /**
     * ✅ Batafsil natijani yaratish
     */
    private TestResultDetailedResponse buildDetailedResult(
            TestSession session,
            List<UserAnswer> userAnswers,
            Map<Long, Question> questionsMap
    ) {
        // ✅ Har bir javob bo'yicha batafsil ma'lumot
        List<AnswerDetailDto> answerDetails = userAnswers.stream()
                .map(ua -> {
                    Question q = ua.getQuestion();
                    // Agar questionsMap'da bo'lmasa, lazy loading orqali olish
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

        // ✅ Foiz hisobi
        double percentage = (session.getCorrectCount() * 100.0) / session.getTotalQuestions();
        boolean passed = percentage >= PASS_THRESHOLD;

        // ✅ Davomiylik hisobi
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

    /**
     * ✅ Test tarixini olish
     */
    @Override
    @Transactional(readOnly = true)
    public List<TestSessionResponse> getHistory(Long userId) {
        log.info("Test tarixi so'raldi: userId={}", userId);

        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(this::toTestSessionResponse)
                .toList();
    }

    /**
     * ✅ Testni bekor qilish
     */
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

    /**
     * ❌ DEPRECATED: Har bir javobni alohida yuborish (eski usul)
     */
    @Deprecated
    @Override
    @Transactional
    public AnswerResultResponse submitAnswer(
            Long userId,
            Long sessionId,
            SubmitAnswerRequest request
    ) {
        log.warn("DEPRECATED: submitAnswer ishlatildi. submitAllAnswers dan foydalaning!");

        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        if (session.isExpired() || session.getStatus() != TestStatus.IN_PROGRESS) {
            throw new BusinessException("Test tugagan yoki bekor qilingan");
        }

        var question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Savol topilmadi"));

        boolean alreadyAnswered = answerRepository.existsByTestSessionIdAndQuestionId(
                sessionId, request.questionId()
        );

        if (alreadyAnswered) {
            throw new BusinessException("Bu savolga javob allaqachon berilgan");
        }

        boolean isCorrect = question.getCorrectAnswer()
                .trim()
                .equalsIgnoreCase(request.answer().trim());

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
            session.setCorrectCount(session.getCorrectCount() + 1);
        } else {
            session.setWrongCount(session.getWrongCount() + 1);
        }

        sessionRepository.save(session);

        return new AnswerResultResponse(
                isCorrect,
                question.getCorrectAnswer(),
                session.getScore()
        );
    }

    /**
     * ❌ DEPRECATED: Oddiy natija
     */
    @Deprecated
    @Override
    @Transactional(readOnly = true)
    public TestResultResponse getResult(Long userId, Long sessionId) {
        log.warn("DEPRECATED: getResult ishlatildi. getResultById dan foydalaning!");

        var session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessiya topilmadi"));

        long correct = session.getCorrectCount();
        double percentage = (correct * 100.0) / session.getTotalQuestions();

        return new TestResultResponse(
                session.getId(),
                session.getTopic(),
                session.getTotalQuestions(),
                (int) correct,
                session.getScore(),
                percentage,
                session.getStartedAt(),
                session.getFinishedAt()
        );
    }

    // ============================================
    // Helper metodlar
    // ============================================

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

    private boolean existsByTestSessionId(Long sessionId) {
        return answerRepository.countByTestSessionId(sessionId) > 0;
    }
}