package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.drivesmart.entity.TestPackage;

import java.util.List;
import java.util.Optional;

public interface TestPackageRepository extends JpaRepository<TestPackage, Long> {

    List<TestPackage> findByIsActiveTrue();

    Optional<TestPackage> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT tp FROM TestPackage tp WHERE tp.topic = :topic AND tp.isActive = true")
    List<TestPackage> findByTopicAndActive(String topic);
}