package uz.drivesmart.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_sessions")
@Getter
@Setter
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private int totalQuestions;

    private int score = 0;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @OneToMany(mappedBy = "testSession")
    private List<UserAnswer> answers = new ArrayList<>();
}
