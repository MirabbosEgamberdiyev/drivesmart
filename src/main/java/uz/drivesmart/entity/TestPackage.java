package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "test_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestPackage extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name; // "Test 1: Yo'l belgilari"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 10000.00 so'm

    @Column(nullable = false)
    private Integer questionCount; // 20 ta savol

    @Column(nullable = false)
    private Integer durationDays; // 30 kun amal qiladi

    @Column(nullable = false)
    private Integer maxAttempts; // 3 ta urinish

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(length = 100)
    private String topic; // Qaysi mavzu bo'yicha
}