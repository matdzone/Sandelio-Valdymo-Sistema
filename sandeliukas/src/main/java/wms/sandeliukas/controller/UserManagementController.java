package wms.sandeliukas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.service.NotificationService;

@Controller
@RequestMapping("/customer/notifications")
public class UserManagementController {

    private final NotificationService notificationService;

    public UserManagementController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String notificationWindowRequest(Model model, HttpSession session) {
        String userEmail = getLoggedInUserEmail(session);
        if (userEmail == null) {
            return "redirect:/login";
        }

        model.addAttribute("notifications", notificationService.selectNotifications(userEmail));
        return "customer/notifications";
    }

    @GetMapping("/settings")
    public String notificationSettingsRequest(Model model, HttpSession session) {
        String userEmail = getLoggedInUserEmail(session);
        if (userEmail == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", notificationService.getNotificationSettings(userEmail));
        return "customer/notification-settings";
    }

    @PostMapping("/settings")
    public String stateSaveNotification(@RequestParam(value = "showSystemNotifications", required = false) Boolean showSystemNotifications,
                                        @RequestParam(value = "showMessageNotifications", required = false) Boolean showMessageNotifications,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String userEmail = getLoggedInUserEmail(session);
        if (userEmail == null) {
            return "redirect:/login";
        }

        notificationService.saveNotificationSettings(
                userEmail,
                Boolean.TRUE.equals(showSystemNotifications),
                Boolean.TRUE.equals(showMessageNotifications)
        );
        redirectAttributes.addFlashAttribute("success", "Pranešimų nustatymai atnaujinti");
        return "redirect:/customer/notifications/settings";
    }

    @PostMapping("/delete")
    public String deleteRequest(@RequestParam("notificationId") Integer notificationId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String userEmail = getLoggedInUserEmail(session);
        if (userEmail == null) {
            return "redirect:/login";
        }

        try {
            notificationService.deleteNotification(notificationId, userEmail);
            redirectAttributes.addFlashAttribute("success", "Pranešimas pašalintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/notifications";
    }

    private String getLoggedInUserEmail(HttpSession session) {
        Object userEmail = session.getAttribute("userEmail");
        return userEmail == null ? null : userEmail.toString();
    }
}
