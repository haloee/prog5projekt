package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.domain.*;
import hu.pte.mik.prog5.prog5projekt.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final BookRepository books;
    private final ListingRepository listings;
    private final AppUserRepository users;

    public ApiController(BookRepository books, ListingRepository listings, AppUserRepository users) {
        this.books = books; this.listings = listings; this.users = users;
    }

    public record NewBook(String title, String author, String isbn, Integer publishedYear, Long createdByUserId) {}
    public record NewListing(Long ownerId, Long bookId, String condition, String type, Integer priceHuf, String note) {}

    @PostMapping("/books")
    public ResponseEntity<?> createBook(@RequestBody NewBook req) {
        var u = users.findById(req.createdByUserId()).orElseThrow();
        var b = books.save(Book.builder()
                .title(req.title()).author(req.author())
                .isbn(req.isbn()).publishedYear(req.publishedYear())
                .createdBy(u).build());
        return ResponseEntity.created(URI.create("/api/books/" + b.getId())).body(b.getId());
    }

    @PostMapping("/listings")
    public ResponseEntity<?> createListing(@RequestBody NewListing req) {
        var owner = users.findById(req.ownerId()).orElseThrow();
        var book  = books.findById(req.bookId()).orElseThrow();
        var l = listings.save(Listing.builder()
                .owner(owner).book(book)
                .condition(req.condition()).type(req.type())
                .priceHuf(req.priceHuf()).note(req.note())
                .build());
        return ResponseEntity.created(URI.create("/api/listings/" + l.getId())).body(l.getId());
    }
}
