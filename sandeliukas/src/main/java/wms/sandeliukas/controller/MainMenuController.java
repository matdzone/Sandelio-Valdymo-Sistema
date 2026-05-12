package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainMenuController {

    @GetMapping("/customer/menu")
    public String mainMenu() {
        return "customer/main-menu";
    }

    @GetMapping("/customer/main-menu")
    public String mainMenuAlt(Model model) {
        return "customer/main-menu";
    }

    @GetMapping("/customer/item-list")
    public String requestItemListWindow() {
        return "redirect:/customer/products";
    }
}
