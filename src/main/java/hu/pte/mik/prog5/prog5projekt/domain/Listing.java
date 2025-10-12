package hu.pte.mik.prog5.prog5projekt.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "listing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // MariaDB reserved szó elkerülése: oszlopnév átnevezve
    @Column(name = "book_condition", nullable = false)
    private String condition; // NEW / LIKE_NEW / GOOD / USED

    // MariaDB reserved szó elkerülése: oszlopnév átnevezve
    @Column(name = "listing_type", nullable = false)
    private String type;      // EXCHANGE / GIVEAWAY / SELL

    private Integer priceHuf;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE / RESERVED / CLOSED

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Listing.java
    @Column(name = "image_url")
    private String imageUrl;

}
