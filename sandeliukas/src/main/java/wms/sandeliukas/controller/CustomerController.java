package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import wms.sandeliukas.repositories.ProductRepository;

@Controller
public class CustomerController {

    private final ProductRepository productRepository;

    public CustomerController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/customer/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "customer/products";
    }
}