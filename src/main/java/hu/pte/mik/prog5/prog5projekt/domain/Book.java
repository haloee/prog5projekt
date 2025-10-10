package hu.pte.mik.prog5.prog5projekt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "book")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String title;
    @Column(nullable = false) private String author;
    private String isbn;
    private Integer publishedYear;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(name="created_at", nullable = false) private Instant createdAt = Instant.now();
}
