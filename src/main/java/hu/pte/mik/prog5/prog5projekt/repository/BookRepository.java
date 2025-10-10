package hu.pte.mik.prog5.prog5projekt.repository;

import hu.pte.mik.prog5.prog5projekt.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> { }
