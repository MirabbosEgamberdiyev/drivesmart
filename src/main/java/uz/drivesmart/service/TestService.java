package uz.drivesmart.service;

import uz.drivesmart.dto.request.SubmitAllAnswersRequest;
import uz.drivesmart.dto.request.SubmitAnswerRequest;
import uz.drivesmart.dto.request.TestRequest;
import uz.drivesmart.dto.response.*;

import java.util.List;

/**
 * Test service interface
 * ✅ ptest.uz formatiga mos metodlar qo'shildi
 */
public interface TestService {

    /**
     * Testni boshlash - barcha savollar to'g'ri javoblar bilan qaytariladi
     * @param userId Foydalanuvchi ID
     * @param request Test parametrlari (mavzu, savollar soni)
     * @return Test sessiyasi va barcha savollar
     */
    TestStartResponse startTest(Long userId, TestRequest request);

//    /**
//     * ❌ DEPRECATED: Har bir javobni alohida yuborish (eski usul)
//     * Mavjud frontend kodlar uchun saqlanadi, lekin yangi loyihalarda ishlatmaslik tavsiya etiladi
//     */
//    @Deprecated(since = "2.0", forRemoval = true)
//    AnswerResultResponse submitAnswer(Long userId, Long sessionId, SubmitAnswerRequest request);

    /**
     * ✅ Barcha javoblarni bir vaqtda yuborish (ptest.uz usuli)
     * @param userId Foydalanuvchi ID
     * @param request Barcha javoblar
     * @return Batafsil natija (har bir savol bo'yicha tahlil)
     */
    TestResultDetailedResponse submitAllAnswers(Long userId, SubmitAllAnswersRequest request);

//    /**
//     * ❌ DEPRECATED: Oddiy natija (eski format)
//     */
//    @Deprecated(since = "2.0", forRemoval = true)
//    TestResultResponse getResult(Long userId, Long sessionId);

    /**
     * ✅ Test natijasini batafsil olish
     * @param userId Foydalanuvchi ID
     * @param sessionId Test sessiya ID
     * @return Har bir savol bo'yicha tahlil
     */
    TestResultDetailedResponse getResultById(Long userId, Long sessionId);

    /**
     * Foydalanuvchining barcha testlar tarixi
     * @param userId Foydalanuvchi ID
     * @return Testlar ro'yxati
     */
    List<TestSessionResponse> getHistory(Long userId);

    /**
     * ✅ Testni vaqtidan oldin tugatish
     * @param userId Foydalanuvchi ID
     * @param sessionId Test sessiya ID
     */
    void abandonTest(Long userId, Long sessionId);
}