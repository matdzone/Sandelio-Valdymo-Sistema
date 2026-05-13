package wms.sandeliukas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.Purchase;
import wms.sandeliukas.service.CartListService;

import java.util.List;

@Controller
@RequestMapping("/customer/cart")
public class CartListController {

    private final CartListService cartListService;

    public CartListController(CartListService cartListService) {
        this.cartListService = cartListService;
    }

    private String requestUsersData(HttpSession session) {
        return (String) session.getAttribute("userEmail");
    }

    @GetMapping
    public String requestCartItemList(HttpSession session, Model model) {
        String buyerEmail = requestUsersData(session);

        model.addAttribute("cartItems", cartListService.selectCartItemsNotBought(buyerEmail));
        model.addAttribute("cartTotal", cartListService.calculateCartTotal(buyerEmail));

        List<Purchase> reservedPurchases = cartListService.requestUsersPurchaseData(buyerEmail);
        model.addAttribute("reservedPurchases", reservedPurchases);

        return "customer/cart-list";
    }

    @PostMapping("/delete")
    public String requestDeleteItem(@RequestParam("cartItemId") Integer cartItemId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String buyerEmail = requestUsersData(session);

        try {
            cartListService.deleteSelectedItem(buyerEmail, cartItemId);
            redirectAttributes.addFlashAttribute("success", "Prekė pašalinta iš krepšelio");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @PostMapping("/change-amount")
    public String requestNewAmount(@RequestParam("cartItemId") Integer cartItemId,
                                   @RequestParam("newAmount") Integer newAmount,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        String buyerEmail = requestUsersData(session);

        try {
            cartListService.setNewAmountForItemInCart(buyerEmail, cartItemId, newAmount);
            redirectAttributes.addFlashAttribute("success", "Kiekis atnaujintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @PostMapping("/reserve")
    public String requestPurchase(HttpSession session, RedirectAttributes redirectAttributes) {
        String buyerEmail = requestUsersData(session);

        try {
            Purchase purchase = cartListService.reserveNewPurchaseWithCartItemsAndDate(buyerEmail);
            redirectAttributes.addFlashAttribute("success",
                    "Pirkimas rezervuotas (ID: " + purchase.getId() + "). Turite 20 minučių apmokėti.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    @PostMapping("/confirm-payment")
    public String confirmPayment(@RequestParam("purchaseId") Integer purchaseId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String buyerEmail = requestUsersData(session);

        try {
            CartListService.PaymentOutcome outcome = cartListService.confirmPayment(buyerEmail, purchaseId);

            if (outcome.isSuccess()) {
                redirectAttributes.addFlashAttribute("success", outcome.getMessage());
                return "redirect:/customer/main-menu";
            } else {
                redirectAttributes.addFlashAttribute("error", outcome.getMessage());
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/cart";
    }
}
