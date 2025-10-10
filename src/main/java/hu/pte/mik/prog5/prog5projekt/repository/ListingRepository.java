package hu.pte.mik.prog5.prog5projekt.repository;

import hu.pte.mik.prog5.prog5projekt.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> { }
