package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_answers")
@Getter
@Setter
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "test_session_id")
    private TestSession testSession;

    @Column(nullable = false)
    private String selectedAnswer;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(nullable = false)
    private LocalDateTime answeredAt;
}