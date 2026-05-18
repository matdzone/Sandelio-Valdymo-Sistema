package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.model.InventoryProductData;
import wms.sandeliukas.service.InventoryManagementService;

@Controller
@RequestMapping("/warehouse")
public class InventoryManagementController {

    private final InventoryManagementService inventoryManagementService;

    public InventoryManagementController(InventoryManagementService inventoryManagementService) {
        this.inventoryManagementService = inventoryManagementService;
    }

    @GetMapping
    public String rackLayoutWindowRequest(Model model) {
        model.addAttribute("items", inventoryManagementService.itemLayoutEditRequest());
        model.addAttribute("products", inventoryManagementService.selectItemRequest());
        return "warehouse/rack-layout";
    }

    @GetMapping("/product")
    public String itemVolumeRequest(@RequestParam("productId") Integer productId, Model model) {
        InventoryProductData productData = inventoryManagementService.itemVolumeRequest(productId);
        if(!inventoryManagementService.receivedDataCheck(productData)){
            throw new RuntimeException("Prekės duomenys neteisingi");
        }
        inventoryManagementService.saveData(model, productData);
        return "warehouse/rack-layout";
    }

    @PostMapping("/assign")
    public String rackDataRequest(@RequestParam("productId") Integer productId,
                                  RedirectAttributes redirectAttributes) {
        try {
            InventoryProductData productData = inventoryManagementService.itemVolumeRequest(productId);
            var rackData = inventoryManagementService.chooseCorrectRack(productData);
            inventoryManagementService.assignmentItemToRack(productData, rackData);
            inventoryManagementService.dataRackSave(rackData);
            inventoryManagementService.dataItemUpdate(productData);
            redirectAttributes.addFlashAttribute("success", "Prekė priskirta stelažui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/warehouse";
    }

    @PostMapping("/correct-by-departure-date")
    public String itemDepartureDatesRequest(RedirectAttributes redirectAttributes) {
        inventoryManagementService.itemDepartureDatesRequest();
        inventoryManagementService.updateLayout();
        redirectAttributes.addFlashAttribute("success", "Išdėstymas pakoreguotas pagal išvežimo datą");
        return "redirect:/warehouse";
    }

    @GetMapping("/manual")
    public String selectItemRequest(@RequestParam(value = "productId", required = false) Integer productId, Model model) {
        model.addAttribute("products", inventoryManagementService.selectItemRequest());
        if (productId != null) {
            model.addAttribute("selectedProductId", productId);
            model.addAttribute("racks", inventoryManagementService.userChoiceRequest(productId));
        }
        return "warehouse/manual-rack-selection";
    }

    @PostMapping("/manual")
    public String rackChoiceRequest(@RequestParam("productId") Integer productId,
                                    @RequestParam("rackId") Integer rackId,
                                    RedirectAttributes redirectAttributes) {
        try {
            inventoryManagementService.rackChoiceRequest(productId, rackId);
            redirectAttributes.addFlashAttribute("success", "Prekė rankiniu būdu priskirta stelažui");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/warehouse";
    }
}
