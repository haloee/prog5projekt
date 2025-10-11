package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.repository.ListingRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {
    private final ListingRepository listings;

    public HomeController(ListingRepository listings) { this.listings = listings; }

    @GetMapping("/")
    public String index(@RequestParam(defaultValue = "") String q,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        var pageData = listings.search(q.isBlank()? null : q,
                (status==null || status.isBlank())? null : status,
                pageable);
        model.addAttribute("page", pageData);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "index";
    }
}
