package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "topic"})
})
@Getter
@Setter
public class UserProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String topic;

    @Column(nullable = false)
    private Integer totalTests = 0;

    @Column(nullable = false)
    private Integer passedTests = 0;

    @Column(nullable = false)
    private Integer totalQuestions = 0;

    @Column(nullable = false)
    private Integer correctAnswers = 0;

    @Column(nullable = false)
    private Double averageScore = 0.0;

    @Column(nullable = false)
    private Double bestScore = 0.0;

    @Column(nullable = false)
    private Integer currentStreak = 0;

    @Column(nullable = false)
    private Integer longestStreak = 0;

    public void updateFromSession(TestSession session) {
        this.totalTests++;
        this.totalQuestions += session.getTotalQuestions();
        this.correctAnswers += session.getCorrectCount();

        double sessionScore = (session.getCorrectCount() * 100.0) / session.getTotalQuestions();
        this.averageScore = ((this.averageScore * (totalTests - 1)) + sessionScore) / totalTests;

        if (sessionScore > this.bestScore) {
            this.bestScore = sessionScore;
        }

        if (sessionScore >= 70.0) {
            this.passedTests++;
            this.currentStreak++;
            if (this.currentStreak > this.longestStreak) {
                this.longestStreak = this.currentStreak;
            }
        } else {
            this.currentStreak = 0;
        }
    }
}