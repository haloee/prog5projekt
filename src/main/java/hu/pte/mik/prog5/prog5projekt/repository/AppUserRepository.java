package hu.pte.mik.prog5.prog5projekt.repository;

import hu.pte.mik.prog5.prog5projekt.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}
