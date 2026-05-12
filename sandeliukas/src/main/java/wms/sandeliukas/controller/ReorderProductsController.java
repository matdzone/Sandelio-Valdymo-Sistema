package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.LowStockItem;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.service.ReorderProductsService;

@Controller
@RequestMapping("/reorder-products")
public class ReorderProductsController {

    private final ReorderProductsService reorderProductsService;

    public ReorderProductsController(ReorderProductsService reorderProductsService) {
        this.reorderProductsService = reorderProductsService;
    }

    // 2. openReorderProductsWindow()
    // 3. requestReorderProductsData()
    // 8. displayReorderProductsList()
    @GetMapping
    public String openReorderProductsWindow(Model model) {
        model.addAttribute("lowStockItems", reorderProductsService.requestReorderProductsData());
        return "reorder-products/list";
    }

    // 9. selectReorderProduct()
    // 10. orderReorderProduct()
    // 1. getMissingProduct()
    // 3. getItemData()
    // 5. displayOrderForm()
    @GetMapping("/{id}/order")
    public String orderReorderProduct(@PathVariable("id") Integer id, Model model) {
        LowStockItem missingProductData = reorderProductsService.getMissingProduct(id);
        Product itemData = reorderProductsService.getItemData(missingProductData.getProduct().getId());

        model.addAttribute("lowStockItem", missingProductData);
        model.addAttribute("product", itemData);

        return "reorder-products/order-form";
    }

    // 6. submitOrderQuantity()
    // 7. orderData()
    // 14. displayReorderProductsList()
    // 15. showReorderProductsList()
    @PostMapping("/{id}/order")
    public String submitOrderQuantity(@PathVariable("id") Integer id,
                                      @RequestParam("orderQuantity") Integer orderQuantity,
                                      RedirectAttributes redirectAttributes) {
        try {
            reorderProductsService.orderData(id, orderQuantity);
            redirectAttributes.addFlashAttribute("success", "Trūkstama prekė užsakyta");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reorder-products";
    }
}