package uz.drivesmart.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.drivesmart.dto.request.TestPackageRequest;
import uz.drivesmart.dto.response.TestPackageDto;
import uz.drivesmart.entity.TestPackage;
import uz.drivesmart.exception.ResourceNotFoundException;
import uz.drivesmart.repository.TestPackageRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPackageService {

    private final TestPackageRepository packageRepository;

    @Transactional(readOnly = true)
    public List<TestPackageDto> getAllPackages() {
        log.info("Admin fetching all packages");
        return packageRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TestPackageDto createPackage(TestPackageRequest request) {
        log.info("Creating new package: {}", request.name());

        TestPackage pkg = new TestPackage();
        pkg.setName(request.name());
        pkg.setDescription(request.description());
        pkg.setPrice(request.price());
        pkg.setQuestionCount(request.questionCount());
        pkg.setDurationDays(request.durationDays());
        pkg.setMaxAttempts(request.maxAttempts());
        pkg.setTopic(request.topic());
        pkg.setIsActive(true);

        pkg = packageRepository.save(pkg);
        log.info("Package created with ID: {}", pkg.getId());

        return toDto(pkg);
    }

    @Transactional
    public TestPackageDto updatePackage(Long id, TestPackageRequest request) {
        log.info("Updating package: {}", id);

        TestPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paket topilmadi"));

        pkg.setName(request.name());
        pkg.setDescription(request.description());
        pkg.setPrice(request.price());
        pkg.setQuestionCount(request.questionCount());
        pkg.setDurationDays(request.durationDays());
        pkg.setMaxAttempts(request.maxAttempts());
        pkg.setTopic(request.topic());

        pkg = packageRepository.save(pkg);
        log.info("Package updated: {}", id);

        return toDto(pkg);
    }

    @Transactional
    public void togglePackageStatus(Long id) {
        log.info("Toggling package status: {}", id);

        TestPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paket topilmadi"));

        pkg.setIsActive(!pkg.getIsActive());
        packageRepository.save(pkg);

        log.info("Package status toggled: {} -> {}", id, pkg.getIsActive());
    }

    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting package: {}", id);

        TestPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paket topilmadi"));

        // Check if any active purchases exist
        // TODO: Add validation here if needed

        packageRepository.delete(pkg);
        log.info("Package deleted: {}", id);
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
