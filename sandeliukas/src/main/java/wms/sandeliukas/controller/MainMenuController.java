package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainMenuController {

    // 1. cartListButton → 2. requestCartListWindow()
    // Peradresuoja į CartListController, kuris valdo krepšelio logiką (žinutės 3–7)
    @GetMapping("/customer/menu")
    public String requestCartListWindow() {
        // 3. cartList() — perduodame į CartList boundary per redirect
        return "redirect:/customer/cart";
    }
}