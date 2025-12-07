package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uz.drivesmart.entity.UserTestAccess;

import java.util.List;
import java.util.Optional;

public interface UserTestAccessRepository extends JpaRepository<UserTestAccess, Long> {

    Optional<UserTestAccess> findByUserIdAndTestPackageId(Long userId, Long packageId);

    @Query("SELECT uta FROM UserTestAccess uta " +
            "WHERE uta.user.id = :userId " +
            "AND uta.isActive = true " +
            "AND uta.expiresAt > CURRENT_TIMESTAMP " +
            "AND uta.remainingAttempts > 0")
    List<UserTestAccess> findActiveAccessByUserId(Long userId);
}