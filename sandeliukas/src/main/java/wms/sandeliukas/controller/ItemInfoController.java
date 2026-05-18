package wms.sandeliukas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.Comment;
import wms.sandeliukas.service.ItemInfoService;

@Controller
@RequestMapping("/customer/products")
public class ItemInfoController {

    private final ItemInfoService itemInfoService;

    public ItemInfoController(ItemInfoService itemInfoService) {
        this.itemInfoService = itemInfoService;
    }

    @GetMapping("/{productId}")
    public String requestItemInformation(@PathVariable Integer productId,
                                         HttpSession session,
                                         Model model) {
        String buyerEmail = (String) session.getAttribute("userEmail");

        model.addAttribute("product", itemInfoService.requestSelectedItemInformation(productId));
        model.addAttribute("isInCart", itemInfoService.requestIsItemInCart(buyerEmail, productId));
        model.addAttribute("comments", itemInfoService.requestItemComments(productId));
        model.addAttribute("buyerEmail", buyerEmail);

        return "customer/item-info";
    }

    @GetMapping("/{productId}/comments/new")
    public String requestNewCommentForm(@PathVariable Integer productId) {
        return "redirect:/customer/products/" + productId;
    }

    @GetMapping("/{productId}/comments/{commentId}/edit")
    public String requestOldCommentForm(@PathVariable Integer productId,
                                        @PathVariable Integer commentId) {
        return "redirect:/customer/products/" + productId;
    }

    @PostMapping("/{productId}/add-to-cart")
    public String requestNewCartItem(@PathVariable Integer productId,
                                     @RequestParam("quantity") Integer quantity,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String buyerEmail = (String) session.getAttribute("userEmail");

        try {
            itemInfoService.addItemToCartRequest(buyerEmail, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Prekė pridėta į krepšelį");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/products/" + productId;
    }

    @PostMapping("/{productId}/comments/new")
    public String requestNewComment(@PathVariable Integer productId,
                                    @RequestParam("text") String text,
                                    @RequestParam("rating") Integer rating,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String buyerEmail = (String) session.getAttribute("userEmail");

        try {
            Comment.validateData(text, rating);
            itemInfoService.recordNewComment(buyerEmail, productId, text, rating);
            redirectAttributes.addFlashAttribute("success", "Komentaras pridėtas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/products/" + productId;
    }

    @PostMapping("/{productId}/comments/{commentId}/edit")
    public String requestEditComment(@PathVariable Integer productId,
                                     @PathVariable Integer commentId,
                                     @RequestParam("text") String text,
                                     @RequestParam("rating") Integer rating,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String buyerEmail = (String) session.getAttribute("userEmail");

        try {
            Comment.validateData(text, rating);
            itemInfoService.editCommentInformation(buyerEmail, commentId, text, rating);
            redirectAttributes.addFlashAttribute("success", "Komentaras atnaujintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/products/" + productId;
    }

    @PostMapping("/{productId}/comments/{commentId}/delete")
    public String requestDeleteComment(@PathVariable Integer productId,
                                       @PathVariable Integer commentId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        String buyerEmail = (String) session.getAttribute("userEmail");

        try {
            itemInfoService.deleteOldComment(buyerEmail, commentId);
            redirectAttributes.addFlashAttribute("success", "Komentaras ištrintas");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/customer/products/" + productId;
    }

}
