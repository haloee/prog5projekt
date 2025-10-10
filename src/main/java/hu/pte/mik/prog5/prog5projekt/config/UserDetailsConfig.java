package hu.pte.mik.prog5.prog5projekt.config;

import hu.pte.mik.prog5.prog5projekt.domain.AppUser;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class UserDetailsConfig {

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(AppUserRepository repo) {
        return username -> {
            AppUser u = repo.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
            return new User(u.getEmail(), u.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole())));
        };
    }
}
