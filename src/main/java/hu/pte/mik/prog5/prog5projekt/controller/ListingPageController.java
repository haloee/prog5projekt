package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.domain.Listing;
import hu.pte.mik.prog5.prog5projekt.dto.NewListingDto;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import hu.pte.mik.prog5.prog5projekt.repository.BookRepository;
import hu.pte.mik.prog5.prog5projekt.repository.ListingRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/listings")
public class ListingPageController {

    private final ListingRepository listings;
    private final BookRepository books;
    private final AppUserRepository users;

    public ListingPageController(ListingRepository listings, BookRepository books, AppUserRepository users) {
        this.listings = listings;
        this.books = books;
        this.users = users;
    }

    /* --------- CREATE --------- */

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new NewListingDto(null, null, "GOOD", "SELL", 0, ""));
        model.addAttribute("users", users.findAll());
        model.addAttribute("books", books.findAll());
        return "listings/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("dto") NewListingDto dto,
                         BindingResult br,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("users", users.findAll());
            model.addAttribute("books", books.findAll());
            return "listings/new";
        }

        var owner = users.findById(dto.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner not found"));
        var book = books.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));

        listings.save(Listing.builder()
                .owner(owner)
                .book(book)
                .condition(dto.condition())
                .type(dto.type())
                .priceHuf(dto.priceHuf())
                .note(dto.note())
                .build());

        return "redirect:/";
    }

    /* --------- EDIT FORM --------- */

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var listing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        var dto = new NewListingDto(
                listing.getOwner() != null ? listing.getOwner().getId() : null,
                listing.getBook()  != null ? listing.getBook().getId()  : null,
                listing.getCondition(),
                listing.getType(),
                listing.getPriceHuf(),
                listing.getNote()
        );

        model.addAttribute("id", id);
        model.addAttribute("dto", dto);
        model.addAttribute("users", users.findAll());
        model.addAttribute("books", books.findAll());
        return "listings/edit";
    }

    /* --------- UPDATE --------- */

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("dto") NewListingDto dto,
                         BindingResult br,
                         Model model) {

        var existing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        if (br.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("users", users.findAll());
            model.addAttribute("books", books.findAll());
            return "listings/edit";
        }

        var owner = users.findById(dto.ownerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner not found"));
        var book = books.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));

        existing.setOwner(owner);
        existing.setBook(book);
        existing.setCondition(dto.condition());  // @Column(name="book_condition") az entitásban
        existing.setType(dto.type());            // @Column(name="listing_type") az entitásban
        existing.setPriceHuf(dto.priceHuf());
        existing.setNote(dto.note());

        listings.save(existing);
        return "redirect:/";
    }

    /* --------- DELETE --------- */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        if (!listings.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }
        listings.deleteById(id);
        return "redirect:/";
    }
}
