package wms.sandeliukas.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import wms.sandeliukas.service.MetricsService;

@Controller
@RequestMapping("/admin/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    public String requestMetricsWindow(HttpSession session, Model model) {
        if (session.getAttribute("userEmail") == null) {
            return "redirect:/login";
        }

        // Greitaveika
        model.addAttribute("heapUsedMb",      metricsService.getHeapUsedMb());
        model.addAttribute("heapMaxMb",        metricsService.getHeapMaxMb());
        model.addAttribute("nonHeapUsedMb",    metricsService.getNonHeapUsedMb());
        model.addAttribute("heapUsagePercent", metricsService.getHeapUsagePercent());
        model.addAttribute("uptimeFormatted",  metricsService.getUptimeFormatted());
        model.addAttribute("totalRequests",    metricsService.getTotalHttpRequests());
        model.addAttribute("avgRequestMs",     String.format("%.1f", metricsService.getAvgHttpRequestMs()));
        model.addAttribute("errorRequests",    metricsService.getErrorHttpRequests());
        model.addAttribute("threadCount",      metricsService.getThreadCount());
        model.addAttribute("processors",       metricsService.getAvailableProcessors());

        // Duomenu kiekis
        model.addAttribute("productCount",    metricsService.getProductCount());
        model.addAttribute("userCount",       metricsService.getUserCount());
        model.addAttribute("purchaseCount",   metricsService.getPurchaseCount());
        model.addAttribute("commentCount",    metricsService.getCommentCount());
        model.addAttribute("cartItemCount",   metricsService.getCartItemCount());

        // Atnaujinimo galimybes
        model.addAttribute("springVersion",   metricsService.getSpringBootVersion());
        model.addAttribute("javaVersion",     metricsService.getJavaVersion());
        model.addAttribute("javaVendor",      metricsService.getJavaVendor());
        model.addAttribute("buildTool",       metricsService.getBuildTool());
        model.addAttribute("updateStrategy",  metricsService.getUpdateStrategy());

        // Saugumas
        model.addAttribute("protectedRoutes", metricsService.getProtectedRouteCount());
        model.addAttribute("authMechanism",   metricsService.getAuthMechanism());
        model.addAttribute("sessionStrategy", metricsService.getSessionStrategy());
        model.addAttribute("passwordStorage", metricsService.getPasswordStorage());

        return "admin/metrics";
    }
}
