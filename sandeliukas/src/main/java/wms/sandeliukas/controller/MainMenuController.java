package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import wms.sandeliukas.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainMenuController {
    private final NotificationService notificationService;

    public MainMenuController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

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
    
    @GetMapping("/customer/notifications")
    public String notificationWindowRequest(Model model, HttpSession session) {
        String userEmail = getLoggedInUserEmail(session);
        return notificationService.notificationList(model, userEmail);
    }

    private String getLoggedInUserEmail(HttpSession session) {
        Object userEmail = session.getAttribute("userEmail");
        return userEmail == null ? null : userEmail.toString();
    }
}
