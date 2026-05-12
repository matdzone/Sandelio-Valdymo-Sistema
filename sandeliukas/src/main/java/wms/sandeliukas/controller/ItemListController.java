package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.repositories.ProductRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemListController {

    private static final int PAGE_SIZE = 40;

    private final ProductRepository productRepository;

    public ItemListController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/customer/products")
    public String requestItemListWindow(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        return requestItemList(page, model);
    }

    @GetMapping("/customer/products/{productId}/view")
    public String requestItemInformationWindow(@PathVariable Integer productId) {
        return "redirect:/customer/products/" + productId;
    }

    private String requestItemList(int page, Model model) {
        List<Product> allProducts = new ArrayList<>(productRepository.findAll());
        sortItems(allProducts);

        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);

        int validPage = getWindowNumber(page, totalPages);

        List<Product> pageProducts;
        if (validPage == 1) {
            pageProducts = selectRandomFirst40(allProducts);
        } else {
            pageProducts = selectRandom40ByNumber(allProducts, validPage);
        }

        generatePageLinks(model, validPage, totalPages, totalProducts);
        model.addAttribute("products", pageProducts);

        return "customer/products";
    }

    private List<Product> selectRandomFirst40(List<Product> allProducts) {
        if (allProducts.isEmpty()) return List.of();
        int toIndex = Math.min(PAGE_SIZE, allProducts.size());
        return allProducts.subList(0, toIndex);
    }

    private List<Product> selectRandom40ByNumber(List<Product> allProducts, int page) {
        int fromIndex = (page - 1) * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allProducts.size());
        if (fromIndex >= allProducts.size()) return List.of();
        return allProducts.subList(fromIndex, toIndex);
    }

    private void sortItems(List<Product> items) {
        items.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    private void generatePageLinks(Model model, int page, int totalPages, int totalProducts) {
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalProducts", totalProducts);
    }

    private int getWindowNumber(int page, int totalPages) {
        if (page < 1) return 1;
        if (totalPages > 0 && page > totalPages) return totalPages;
        return page;
    }
}
