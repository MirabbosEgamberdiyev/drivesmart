package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import uz.drivesmart.enums.TestStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_sessions")
@Getter
@Setter
public class TestSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String topic;

    private Integer totalQuestions;
    private Integer score = 0;
    private Integer correctCount = 0;
    private Integer wrongCount = 0;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private Integer durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestStatus status = TestStatus.IN_PROGRESS;

    @OneToMany(mappedBy = "testSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> answers = new ArrayList<>();

    public boolean isExpired() {
        if (finishedAt != null) return true;
        return startedAt.plusMinutes(durationMinutes).isBefore(LocalDateTime.now());
    }
}
