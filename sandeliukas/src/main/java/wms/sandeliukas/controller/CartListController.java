package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.Purchase;
import wms.sandeliukas.service.CartListService;

@Controller
@RequestMapping("/customer/cart")
public class CartListController {

    private final CartListService cartListService;

    public CartListController(CartListService cartListService) {
        this.cartListService = cartListService;
    }

    // 3. cartList() → 4. requestCartItemList() → 5. selectCartItemsNotBought → 7. windowWithCartItems
    @GetMapping
    public String requestCartItemList(Model model) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        // 5. selectCartItemsNotBought
        model.addAttribute("cartItems", cartListService.selectCartItemsNotBought(buyerEmail));
        model.addAttribute("cartTotal", cartListService.calculateCartTotal(buyerEmail));

        // 7. windowWithCartItems
        return "customer/cart-list";
    }

    // 2. requestDeleteItem() → 3. deleteSelectedItem → 4. deleteSuccess → 5. displayCorrectedCartItemsWindow
    @PostMapping("/delete")
    public String requestDeleteItem(@RequestParam("cartItemId") Integer cartItemId,
                                    RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
            // 3. deleteSelectedItem
            cartListService.deleteSelectedItem(buyerEmail, cartItemId);
            // 4. deleteSuccess
            redirectAttributes.addFlashAttribute("success", "Prekė pašalinta iš krepšelio");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // 5. displayCorrectedCartItemsWindow → 6. correctedCartItemsWindow
        return "redirect:/customer/cart";
    }

    // 8. requestNewAmount() → 9. setNewAmountForItemInCart → 10. changeAmountSuccess → 11. displayCorrectedCartItemsWindow
    @PostMapping("/change-amount")
    public String requestNewAmount(@RequestParam("cartItemId") Integer cartItemId,
                                   @RequestParam("newAmount") Integer newAmount,
                                   RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
            // 9. setNewAmountForItemInCart
            cartListService.setNewAmountForItemInCart(buyerEmail, cartItemId, newAmount);
            // 10. changeAmountSuccess
            redirectAttributes.addFlashAttribute("success", "Kiekis atnaujintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // 11. displayCorrectedCartItemsWindow → 12. correctedCartItemsWindow
        return "redirect:/customer/cart";
    }

    // 9. requestPurchase() → ref: Rezervuoti pirkimą → ref: Patvirtinti pirkimą (mokėjimas)
    @PostMapping("/reserve")
    public String requestPurchase(RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
            // ref: Rezervuoti pirkimą
            // 1. getCartItemsNotBought → 3. reserveNewPurchaseWithCartItemsAndDate → 4. reservationSuccess
            Purchase purchase = cartListService.reserveNewPurchaseWithCartItemsAndDate(buyerEmail);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Pirkimas rezervuotas. Purchase ID: " + purchase.getId()
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/cart";
    }
}