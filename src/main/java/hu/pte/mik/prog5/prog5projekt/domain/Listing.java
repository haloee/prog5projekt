package hu.pte.mik.prog5.prog5projekt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "listing")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Listing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="book_id", nullable=false)
    private Book book;

    @Column(name="condition", nullable=false) private String condition; // NEW/LIKE_NEW/GOOD/USED
    @Column(name="type", nullable=false) private String type;           // EXCHANGE/GIVEAWAY/SELL
    private Integer priceHuf;                                          // ha SELL
    @Column(nullable=false) private String status = "ACTIVE";           // ACTIVE/RESERVED/CLOSED
    @Lob private String note;
    @Column(name="created_at", nullable = false) private Instant createdAt = Instant.now();
}
