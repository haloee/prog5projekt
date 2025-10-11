package hu.pte.mik.prog5.prog5projekt.repository;

import hu.pte.mik.prog5.prog5projekt.domain.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    @EntityGraph(attributePaths = {"book","owner"})
    @Query("""
         SELECT l FROM Listing l
         WHERE (:q IS NULL OR LOWER(l.book.title) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.owner.displayName) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.status) LIKE LOWER(CONCAT('%', :q, '%')))
         AND (:status IS NULL OR l.status = :status)
         """)
    Page<Listing> search(@Param("q") String q,
                         @Param("status") String status,
                         Pageable pageable);
}
