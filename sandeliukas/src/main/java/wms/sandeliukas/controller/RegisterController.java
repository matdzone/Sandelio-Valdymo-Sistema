package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.service.AuthService;

@Controller
public class RegisterController {

    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String registerRequest() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerFormRequest(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String repeatedPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            authService.register(firstName, lastName, email, password, repeatedPassword);
            redirectAttributes.addFlashAttribute("success", "Registracija sėkminga. Dabar galite prisijungti");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("firstName", firstName);
            redirectAttributes.addFlashAttribute("lastName", lastName);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/register";
        }
    }
}
