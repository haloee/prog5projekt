package hu.pte.mik.prog5.prog5projekt.controller;

import hu.pte.mik.prog5.prog5projekt.repository.ListingRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    private final ListingRepository listings;
    public PageController(ListingRepository listings) { this.listings = listings; }

    @GetMapping("/welcome")
    public String index(Model model) {
        model.addAttribute("listings", listings.findAll());
        return "index";
    }
}
