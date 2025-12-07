package uz.drivesmart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.response.UserAccessDto;
import uz.drivesmart.entity.UserTestAccess;
import uz.drivesmart.exception.BusinessException;
import uz.drivesmart.repository.UserTestAccessRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestAccessService {

    private final UserTestAccessRepository accessRepository;

    /**
     * Foydalanuvchi test boshlay oladimi?
     */
    @Transactional(readOnly = true)
    public boolean canAccessTest(Long userId, Long packageId) {
        return accessRepository.findByUserIdAndTestPackageId(userId, packageId)
                .map(UserTestAccess::hasAccess)
                .orElse(false);
    }

    /**
     * Foydalanuvchi barcha active access'lari
     */
    @Transactional(readOnly = true)
    public List<UserAccessDto> getUserActiveAccess(Long userId) {
        log.info("Fetching active access for user: {}", userId);
        return accessRepository.findActiveAccessByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Test boshlashda urinishni kamaytirish
     */
    @Transactional
    public void consumeAttempt(Long userId, Long packageId) {
        log.info("Consuming attempt for user: {}, package: {}", userId, packageId);

        UserTestAccess access = accessRepository
                .findByUserIdAndTestPackageId(userId, packageId)
                .orElseThrow(() -> new BusinessException("Kirish huquqi yo'q"));

        if (!access.hasAccess()) {
            throw new BusinessException("Test boshlash uchun kirish huquqi yo'q");
        }

        access.setRemainingAttempts(access.getRemainingAttempts() - 1);
        accessRepository.save(access);

        log.info("Attempt consumed. Remaining: {}", access.getRemainingAttempts());
    }

    private UserAccessDto toDto(UserTestAccess access) {
        return new UserAccessDto(
                access.getId(),
                access.getTestPackage().getId(),
                access.getTestPackage().getName(),
                access.getRemainingAttempts(),
                access.getExpiresAt(),
                access.hasAccess()
        );
    }
}
