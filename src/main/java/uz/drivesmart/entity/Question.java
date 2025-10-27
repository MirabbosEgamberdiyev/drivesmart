package uz.drivesmart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
public class Question extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @ElementCollection
    @CollectionTable(
            name = "question_options",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "option", nullable = false)
    @OrderColumn(name = "option_order")
    private List<String> options;

    @Column(nullable = false, length = 500)
    private String correctAnswer;

    // ✅ To'g'ri javob izohi (nima uchun to'g'ri)
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false, length = 100)
    private String topic;

    // Rasm yo'li
    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "image_content_type", length = 50)
    private String imageContentType;

    @Column(name = "image_size")
    private Long imageSize;

    // Business logic
    public boolean hasImage() {
        return imagePath != null && !imagePath.isBlank();
    }

    public boolean hasExplanation() {
        return explanation != null && !explanation.isBlank();
    }
}