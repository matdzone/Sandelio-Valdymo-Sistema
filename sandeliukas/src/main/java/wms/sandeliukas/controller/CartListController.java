package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
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

    // 2. DeleteItemRequest
    @PostMapping("/delete")
    public String deleteItemRequest(@RequestParam("cartItemId") Integer cartItemId,
                                    RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
            // 3. DeleteSelectedItem
            cartListService.deleteSelectedItem(buyerEmail, cartItemId);
            // 4. DeleteSuccess
            redirectAttributes.addFlashAttribute("success", "Prekė pašalinta iš krepšelio");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // 5. DisplayCorrectedCartItemsWindow
        return "redirect:/customer/cart";
    }

    // 8. NewAmountRequest
    @PostMapping("/change-amount")
    public String newAmountRequest(@RequestParam("cartItemId") Integer cartItemId,
                                   @RequestParam("newAmount") Integer newAmount,
                                   RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
            // 9. SetNewAmountForItemInCart
            cartListService.setNewAmountForItemInCart(buyerEmail, cartItemId, newAmount);
            // 10. ChangeAmountSuccess
            redirectAttributes.addFlashAttribute("success", "Kiekis atnaujintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        // 11. DisplayCorrectedCartItemsWindow
        return "redirect:/customer/cart";
    }

    // 1. GetCartItemsNotBought / 3. ReserveNewPurchaseWithCartItemsAndDate
    @PostMapping("/reserve")
    public String reservePurchase(RedirectAttributes redirectAttributes) {
        String buyerEmail = "laura.vaitkute@gmail.com";

        try {
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