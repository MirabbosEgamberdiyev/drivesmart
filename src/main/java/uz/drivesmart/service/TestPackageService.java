package uz.drivesmart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.response.TestPackageDto;
import uz.drivesmart.entity.TestPackage;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.TestPackageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestPackageService {

    private final TestPackageRepository packageRepository;

    public List<TestPackageDto> getAllActivePackages() {
        log.info("Fetching all active test packages");
        return packageRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TestPackageDto getPackageById(Long id) {
        log.info("Fetching package by ID: {}", id);
        TestPackage pkg = packageRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test paketi topilmadi"));
        return toDto(pkg);
    }

    public List<TestPackageDto> getPackagesByTopic(String topic) {
        log.info("Fetching packages by topic: {}", topic);
        return packageRepository.findByTopicAndActive(topic)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TestPackageDto toDto(TestPackage pkg) {
        return new TestPackageDto(
                pkg.getId(),
                pkg.getName(),
                pkg.getDescription(),
                pkg.getPrice(),
                pkg.getQuestionCount(),
                pkg.getDurationDays(),
                pkg.getMaxAttempts(),
                pkg.getTopic(),
                pkg.getIsActive()
        );
    }
}
