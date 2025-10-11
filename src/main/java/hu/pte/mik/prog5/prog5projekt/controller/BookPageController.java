package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.domain.Book;
import hu.pte.mik.prog5.prog5projekt.repository.AppUserRepository;
import hu.pte.mik.prog5.prog5projekt.repository.BookRepository;
import hu.pte.mik.prog5.prog5projekt.dto.NewBookDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/books")
public class BookPageController {
    private final BookRepository books;
    private final AppUserRepository users;

    public BookPageController(BookRepository books, AppUserRepository users) {
        this.books = books; this.users = users;
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new NewBookDto("", "", "", 2000, 0L));
        model.addAttribute("users", users.findAll());
        return "books/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("dto") NewBookDto dto, BindingResult br, Model model) {
        if (br.hasErrors()) { model.addAttribute("users", users.findAll()); return "books/new"; }
        var u = users.findById(dto.createdByUserId()).orElseThrow();
        books.save(Book.builder()
                .title(dto.title()).author(dto.author())
                .isbn(dto.isbn()).publishedYear(dto.publishedYear())
                .createdBy(u).build());
        return "redirect:/";
    }
}
