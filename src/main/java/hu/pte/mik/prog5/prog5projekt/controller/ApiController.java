package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.domain.Book;
import hu.pte.mik.prog5.prog5projekt.domain.Listing;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import hu.pte.mik.prog5.prog5projekt.repository.BookRepository;
import hu.pte.mik.prog5.prog5projekt.repository.ListingRepository;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final BookRepository books;
    private final ListingRepository listings;
    private final AppUserRepository users;

    public ApiController(BookRepository books, ListingRepository listings, AppUserRepository users) {
        this.books = books;
        this.listings = listings;
        this.users = users;
    }

    /* =========================================
       ===============  DTO-k  =================
       ========================================= */

    // BOOK
    public record NewBook(String title, String author, String isbn, Integer publishedYear, Long createdByUserId) {}
    public record UpsertBook(String title, String author, String isbn, Integer publishedYear) {}
    public record BookDto(Long id, String title, String author, String isbn, Integer publishedYear, Long createdByUserId) {}

    // LISTING
    public record NewListing(Long ownerId, Long bookId, String condition, String type, Integer priceHuf, String note) {}
    public record UpsertListing(Long ownerId, Long bookId, String condition, String type, Integer priceHuf, String note, String imageUrl) {}
    public record StatusChange(String status) {}

    public record ListingDto(
            Long id,
            Long ownerId, String ownerName,
            Long bookId, String bookTitle,
            String condition, String type, String status,
            Integer priceHuf, String note, String imageUrl,
            Instant createdAt
    ) {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}

    /* =========================================
       ==============  MAPPEREK  ===============
       ========================================= */

    private static BookDto toDto(Book b) {
        return new BookDto(
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                b.getPublishedYear(),
                b.getCreatedBy() != null ? b.getCreatedBy().getId() : null
        );
    }

    private static ListingDto toDto(Listing l) {
        return new ListingDto(
                l.getId(),
                l.getOwner() != null ? l.getOwner().getId() : null,
                l.getOwner() != null ? l.getOwner().getDisplayName() : null,
                l.getBook()  != null ? l.getBook().getId() : null,
                l.getBook()  != null ? l.getBook().getTitle() : null,
                l.getCondition(), l.getType(), l.getStatus(),
                l.getPriceHuf(), l.getNote(), l.getImageUrl(),
                l.getCreatedAt()
        );
    }

    private static <T> PageResponse<T> toPage(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }

    /* =========================================
       ===============  BOOK API  ==============
       ========================================= */

    @PostMapping("/books")
    public ResponseEntity<?> createBook(@RequestBody NewBook req) {
        var u = users.findById(req.createdByUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        var b = books.save(Book.builder()
                .title(req.title())
                .author(req.author())
                .isbn(req.isbn())
                .publishedYear(req.publishedYear())
                .createdBy(u)
                .build());
        return ResponseEntity.created(URI.create("/api/books/" + b.getId())).body(b.getId());
    }

    @GetMapping("/books/{id}")
    public BookDto getBook(@PathVariable Long id) {
        var b = books.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return toDto(b);
    }

    @GetMapping("/books")
    public PageResponse<BookDto> listBooks(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "title") String sort,
                                           @RequestParam(defaultValue = "ASC") Sort.Direction dir) {
        Page<Book> p = books.findAll(PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(dir, sort)));
        return toPage(p.map(ApiController::toDto));
    }

    @PutMapping("/books/{id}")
    public BookDto updateBook(@PathVariable Long id, @RequestBody UpsertBook req) {
        var b = books.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        b.setTitle(req.title());
        b.setAuthor(req.author());
        b.setIsbn(req.isbn());
        b.setPublishedYear(req.publishedYear());
        return toDto(books.save(b));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (!books.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        books.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================================
       ==============  LISTING API  =============
       ========================================= */

    @PostMapping("/listings")
    public ResponseEntity<?> createListing(@RequestBody NewListing req) {
        var owner = users.findById(req.ownerId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner not found"));
        var book  = books.findById(req.bookId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));
        var l = listings.save(Listing.builder()
                .owner(owner).book(book)
                .condition(req.condition()).type(req.type())
                .priceHuf(req.priceHuf()).note(req.note())
                .build());
        return ResponseEntity.created(URI.create("/api/listings/" + l.getId())).body(l.getId());
    }

    @GetMapping("/listings/{id}")
    public ListingDto getListing(@PathVariable Long id) {
        var l = listings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        return toDto(l);
    }

    /**
     * Paginált kereső:
     * - q: keresés könyvcímre / owner displayName-re / statusra (ListingRepository.search már tudja)
     * - status: ACTIVE | RESERVED | CLOSED (vagy üres/null = bármi)
     */
    @GetMapping("/listings")
    public PageResponse<ListingDto> searchListings(@RequestParam(required = false) String q,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size,
                                                   @RequestParam(defaultValue = "createdAt") String sort,
                                                   @RequestParam(defaultValue = "DESC") Sort.Direction dir) {
        Page<Listing> p = listings.search(
                (q == null || q.isBlank()) ? null : q,
                (status == null || status.isBlank()) ? null : status,
                PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(dir, sort))
        );
        return toPage(p.map(ApiController::toDto));
    }

    @PutMapping("/listings/{id}")
    public ListingDto updateListing(@PathVariable Long id, @RequestBody UpsertListing req) {
        var l = listings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        var owner = users.findById(req.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner not found"));
        var book  = books.findById(req.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));

        l.setOwner(owner);
        l.setBook(book);
        l.setCondition(req.condition());
        l.setType(req.type());
        l.setPriceHuf(req.priceHuf());
        l.setNote(req.note());
        l.setImageUrl(req.imageUrl()); // opcionális URL (a képfeltöltés a webes formban történik)

        return toDto(listings.save(l));
    }

    /** Csak státusz váltás (kényelmes API a kliensnek) */
    @PatchMapping("/listings/{id}/status")
    public ListingDto changeStatus(@PathVariable Long id, @RequestBody StatusChange req) {
        var l = listings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        String s = req.status();
        if (!( "ACTIVE".equals(s) || "RESERVED".equals(s) || "CLOSED".equals(s) )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        l.setStatus(s);
        return toDto(listings.save(l));
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> deleteListing(@PathVariable Long id) {
        if (!listings.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        listings.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
