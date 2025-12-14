package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.domain.Listing;
import hu.pte.mik.prog5.prog5projekt.dto.NewListingDto;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import hu.pte.mik.prog5.prog5projekt.repository.BookRepository;
import hu.pte.mik.prog5.prog5projekt.repository.ListingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;


    private String currentUsernameOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof UserDetails ud) return ud.getUsername();
        if (p instanceof String s) return s;
        return null;
    }

    private hu.pte.mik.prog5.prog5projekt.domain.AppUser currentUserOr401() {
        String username = currentUsernameOrNull();
        if (username == null || username.equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login required");
        }
        return users.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
    }

    private void checkOwnerOrAdmin(Listing listing) {
        if (listing.getOwner() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (isAdmin()) return;
        var me = currentUserOr401();
        if (!listing.getOwner().getId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your listing");
        }
    }



    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new NewListingDto(null, null, "GOOD", "SELL", 0, ""));
        model.addAttribute("books", books.findAll());
        return "listings/new";
    }

    @PostMapping(
            consumes = {"application/x-www-form-urlencoded", "multipart/form-data"}
    )
    public String create(@Valid @ModelAttribute("dto") NewListingDto dto,
                         BindingResult br,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Model model) throws IOException {

        var me = currentUserOr401(); // owner = bejelentkezett user

        if (br.hasErrors()) {
            model.addAttribute("books", books.findAll());
            return "listings/new";
        }

        var book = books.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));


        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            Files.createDirectories(Path.of(uploadDir)); // @Value-ból jön
            String original = StringUtils.cleanPath(image.getOriginalFilename() == null ? "" : image.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot); // pl. ".jpg"
            String filename = UUID.randomUUID() + ext;
            Path target = Path.of(uploadDir, filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            imageUrl = "/uploads/" + filename; // így szolgáljuk ki statikusan
        }

        listings.save(Listing.builder()
                .owner(me)
                .book(book)
                .condition(dto.condition())
                .type(dto.type())
                .priceHuf(dto.priceHuf())
                .note(dto.note())
                .createdAt(Instant.now())  // <-- javítva
                .imageUrl(imageUrl)
                .build());

        return "redirect:/";
    }




    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var listing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        checkOwnerOrAdmin(listing);

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
        model.addAttribute("books", books.findAll());
        model.addAttribute("currentImageUrl", listing.getImageUrl());


        model.addAttribute("isAdmin", isAdmin());
        model.addAttribute("ownerName",
                listing.getOwner() != null ? listing.getOwner().getDisplayName() : "—");
        if (isAdmin()) {

            model.addAttribute("users", users.findAll());
        }


        return "listings/edit";
    }




    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("dto") NewListingDto dto,
                         BindingResult br,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
                         Model model) throws IOException {

        var existing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        checkOwnerOrAdmin(existing);

        if (br.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("books", books.findAll());
            model.addAttribute("currentImageUrl", existing.getImageUrl());
            // >>> ugyanazok az extra attribútumok hiba esetén is
            model.addAttribute("isAdmin", isAdmin());
            model.addAttribute("ownerName",
                    existing.getOwner() != null ? existing.getOwner().getDisplayName() : "—");
            if (isAdmin()) {
                model.addAttribute("users", users.findAll());
            }
            // <<<
            return "listings/edit";
        }


        if (isAdmin()) {
            var newOwner = users.findById(dto.ownerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner not found"));
            existing.setOwner(newOwner);
        }

        var book = books.findById(dto.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found"));

        existing.setBook(book);
        existing.setCondition(dto.condition());
        existing.setType(dto.type());
        existing.setPriceHuf(dto.priceHuf());
        existing.setNote(dto.note());

        // kép frissítése
        if (removeImage) {
            existing.setImageUrl(null);
        } else if (image != null && !image.isEmpty()) {
            Files.createDirectories(Path.of(uploadDir));
            String original = StringUtils.cleanPath(image.getOriginalFilename() == null ? "" : image.getOriginalFilename());
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String filename = UUID.randomUUID() + ext;
            Path target = Path.of(uploadDir, filename);
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            existing.setImageUrl("/uploads/" + filename);
        }

        listings.save(existing);
        return "redirect:/";
    }


    /* --------- DELETE --------- */

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        var existing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        checkOwnerOrAdmin(existing);

        listings.deleteById(id);
        return "redirect:/";
    }

    /* --- SHOW --- */
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        var listing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        model.addAttribute("listing", listing);
        return "listings/show";
    }

    /* --- STATUS CHANGE --- */
    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false, defaultValue = "false") boolean fromShow) {
        var listing = listings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));

        checkOwnerOrAdmin(listing);

        switch (status) {
            case "ACTIVE", "RESERVED", "CLOSED" -> listing.setStatus(status);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        listings.save(listing);

        return fromShow ? "redirect:/listings/%d".formatted(id) : "redirect:/";
    }

    /* --- MY LISTINGS --- */
    @GetMapping("/me")
    public String myListings(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {
        var me = currentUserOr401();
        Page<Listing> p = listings.findByOwnerId(
                me.getId(),
                PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        model.addAttribute("page", p);
        model.addAttribute("me", me);
        return "listings/my";
    }
}
