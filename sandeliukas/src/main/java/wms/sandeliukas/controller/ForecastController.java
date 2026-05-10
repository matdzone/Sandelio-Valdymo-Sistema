package wms.sandeliukas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import wms.sandeliukas.service.ForecastService;

@Controller
@RequestMapping("/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    public String requestForecastWindow(Model model) {
        forecastService.requestForecastWindow();
        model.addAttribute("forecasts", forecastService.getForecastData());
        return "forecast/forecast-window";
    }

    @PostMapping("/update")
    public String submitForecastChanges(@RequestParam Integer forecastId,
                                        @RequestParam Integer averageDemand,
                                        @RequestParam Double seasonalityCoeff,
                                        @RequestParam Integer reorderPoint,
                                        @RequestParam Integer safetyStock,
                                        @RequestParam Integer stockPosition,
                                        @RequestParam Integer recommendedQuantity,
                                        RedirectAttributes redirectAttributes) {
        try {
            forecastService.updateForecastData(
                    forecastId,
                    averageDemand,
                    seasonalityCoeff,
                    reorderPoint,
                    safetyStock,
                    stockPosition,
                    recommendedQuantity
            );

            redirectAttributes.addFlashAttribute("success", "Prognozė atnaujinta");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/forecast";
    }
}