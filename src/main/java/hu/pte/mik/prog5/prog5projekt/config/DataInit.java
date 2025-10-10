package hu.pte.mik.prog5.prog5projekt.config;

import hu.pte.mik.prog5.prog5projekt.domain.AppUser;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInit {
    @Bean
    CommandLineRunner initUsers(AppUserRepository users, PasswordEncoder pe) {
        return args -> {
            users.findByEmail("admin@example.com").orElseGet(() ->
                    users.save(AppUser.builder()
                            .email("admin@example.com")
                            .password(pe.encode("admin123"))
                            .displayName("Admin")
                            .role("ADMIN")
                            .build())
            );
            users.findByEmail("user@example.com").orElseGet(() ->
                    users.save(AppUser.builder()
                            .email("user@example.com")
                            .password(pe.encode("user123"))
                            .displayName("User")
                            .role("USER")
                            .build())
            );
        };
    }
}
