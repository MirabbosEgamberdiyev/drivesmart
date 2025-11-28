package uz.drivesmart.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "question_statistics")
@Getter
@Setter
public class QuestionStatistics extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private Question question;

    @Column(nullable = false)
    private Long totalAttempts = 0L;

    @Column(nullable = false)
    private Long correctAttempts = 0L;

    @Column(nullable = false)
    private Double successRate = 0.0;

    public void recordAnswer(boolean isCorrect) {
        this.totalAttempts++;
        if (isCorrect) {
            this.correctAttempts++;
        }
        this.successRate = (correctAttempts * 100.0) / totalAttempts;
    }
}