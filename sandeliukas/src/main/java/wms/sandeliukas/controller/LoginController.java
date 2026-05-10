package wms.sandeliukas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.User;
import wms.sandeliukas.service.AuthService;

@Controller
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("userEmail") != null) {
            return "redirect:/customer/menu";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginWindow() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String loginData(@RequestParam String email,
                            @RequestParam String password,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = authService.login(email, password);
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getFirstName());
            redirectAttributes.addFlashAttribute("success", "Sėkmingai prisijungėte");
            return "redirect:/customer/menu";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Atsijungėte nuo sistemos");
        return "redirect:/login";
    }
}
