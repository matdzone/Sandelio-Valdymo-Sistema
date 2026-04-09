package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import wms.sandeliukas.service.CartListService;

@Controller
public class MainMenuController {

    private final CartListService cartListService;

    public MainMenuController(CartListService cartListService) {
        this.cartListService = cartListService;
    }

    // 2. CartListWindowRequest
    @GetMapping("/customer/cart")
    public String cartListWindowRequest(Model model) {
        String buyerEmail = "laura.vaitkute@gmail.com"; // kol kas testinis vartotojas

        // 4. RequestCartItemList -> CartListController logika UML prasme
        model.addAttribute("cartItems", cartListService.selectCartItemsNotBought(buyerEmail));
        model.addAttribute("cartTotal", cartListService.calculateCartTotal(buyerEmail));

        // 7. WindowWithCartItems
        return "customer/cart-list";
    }
}