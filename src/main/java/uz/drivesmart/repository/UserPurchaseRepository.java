package uz.drivesmart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.drivesmart.entity.UserPurchase;
import uz.drivesmart.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

public interface UserPurchaseRepository extends JpaRepository<UserPurchase, Long> {

    Optional<UserPurchase> findByTransactionId(String transactionId);

    List<UserPurchase> findByUserIdOrderByPurchasedAtDesc(Long userId);

    @Query("SELECT up FROM UserPurchase up WHERE up.user.id = :userId AND up.status = :status")
    List<UserPurchase> findByUserIdAndStatus(@Param("userId") Long userId,
                                             @Param("status") PaymentStatus status);

    @Query("SELECT up FROM UserPurchase up WHERE up.status = :status ORDER BY up.purchasedAt DESC")
    List<UserPurchase> findByStatusOrderByPurchasedAtDesc(@Param("status") PaymentStatus status);

    boolean existsByUserIdAndTestPackageIdAndStatus(Long userId, Long packageId, PaymentStatus status);
}