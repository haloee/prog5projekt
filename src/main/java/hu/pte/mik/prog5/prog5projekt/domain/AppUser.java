package hu.pte.mik.prog5.prog5projekt.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;


@Entity @Table(name = "app_user")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String password;
    @Column(name="display_name", nullable = false) private String displayName;
    @Column(nullable = false) private String role; // USER / ADMIN
    @Column(name="created_at", nullable = false) private Instant createdAt = Instant.now();
}

