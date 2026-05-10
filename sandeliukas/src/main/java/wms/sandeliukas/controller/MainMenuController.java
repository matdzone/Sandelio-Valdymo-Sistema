package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainMenuController {

    @GetMapping("/customer/menu")
    public String mainMenu() {
        return "customer/main-menu";
    }
}
