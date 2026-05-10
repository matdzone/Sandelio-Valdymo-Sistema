package wms.sandeliukas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.Forecast;
import wms.sandeliukas.model.LowStockItem;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.repositories.ForecastRepository;
import wms.sandeliukas.repositories.LowStockItemRepository;
import wms.sandeliukas.repositories.ProductRepository;
import wms.sandeliukas.repositories.OrderRepository;
import java.util.concurrent.CompletableFuture;

import java.time.LocalDate;
import java.util.List;

@Service
public class ForecastService {

    private final ForecastRepository forecastRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final LowStockItemRepository lowStockItemRepository;

    public ForecastService(ForecastRepository forecastRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository,
                           LowStockItemRepository lowStockItemRepository) {
        this.forecastRepository = forecastRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.lowStockItemRepository = lowStockItemRepository;
    }

    public List<Forecast> getForecastData() {
        return forecastRepository.findAll();
    }


    public void requestForecastWindow() {
        analyzeSalesHistory();
        determineSeasonality();
        calculateInventoryBalance();

        CompletableFuture<Void> determineMissingProducts =
                CompletableFuture.runAsync(() -> determineMissingProductsByReorderPoint());

        CompletableFuture<Void> planReplenishment =
                CompletableFuture.runAsync(() -> planInventoryReplenishment());

        CompletableFuture.allOf(determineMissingProducts, planReplenishment).join();
    }

    @Transactional
    public void analyzeSalesHistory() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            Integer totalSold = orderRepository.getTotalSold(product.getId());
            Integer months = orderRepository.countSalesMonths(product.getId());

            int averageDemand = 0;

            if (months != null && months > 0) {
                averageDemand = (int) Math.round(totalSold / (double) months);
            }

            forecast.setAverageDemand(averageDemand);
            forecastRepository.save(forecast);
        }
    }

    @Transactional
    public void determineSeasonality() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            Integer monthCount = orderRepository.countSalesMonths(product.getId());

            if (monthCount == null || monthCount < 6) {
                forecast.setSeasonalityCoeff(1.0);
                forecastRepository.save(forecast);
                continue;
            }

            Integer totalSold = orderRepository.getTotalSold(product.getId());
            Integer periodSold = orderRepository.getPeriodSold(
                    product.getId(),
                    forecast.getPeriodStart(),
                    forecast.getPeriodEnd()
            );

            double overallAverage = totalSold / (double) monthCount;

            long periodMonths = java.time.temporal.ChronoUnit.MONTHS.between(
                    forecast.getPeriodStart().withDayOfMonth(1),
                    forecast.getPeriodEnd().withDayOfMonth(1)
            ) + 1;

            double periodAverage = periodSold / (double) periodMonths;

            double coefficient;

            if (overallAverage == 0) {
                coefficient = 1.0;
            } else {
                coefficient = periodAverage / overallAverage;
            }

            coefficient = Math.round(coefficient * 100.0) / 100.0;

            forecast.setSeasonalityCoeff(coefficient);
            forecastRepository.save(forecast);
        }
    }

    @Transactional
    public void calculateInventoryBalance() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            int currentStock = product.getCurrentStock() != null ? product.getCurrentStock() : 0;
            Integer incoming = orderRepository.incomingStock(product.getId());
            int incomingStock = incoming != null ? incoming : 0;

            int stockPosition = currentStock + incomingStock;

            forecast.setStockPosition(stockPosition);
            forecastRepository.save(forecast);
        }
    }

    @Transactional
    public void determineMissingProductsByReorderPoint() {
        List<Forecast> forecasts = forecastRepository.findAll();

        for (Forecast forecast : forecasts) {
            Product product = forecast.getProduct();

            int averageDemand = forecast.getAverageDemand() != null
                    ? forecast.getAverageDemand()
                    : 0;

            int stockPosition = forecast.getStockPosition() != null
                    ? forecast.getStockPosition()
                    : 0;

            int safetyStock = forecast.getSafetyStock() != null
                    ? forecast.getSafetyStock()
                    : 0;

            Double leadTimeValue = orderRepository.averageLeadTimeDays(product.getId());
            int leadTime = leadTimeValue != null
                    ? (int) Math.round(leadTimeValue)
                    : 0;

            int reorderPoint = averageDemand * leadTime + safetyStock;

            forecast.setReorderPoint(reorderPoint);
            forecastRepository.save(forecast);

            if (stockPosition < reorderPoint) {
                createMissingProduct(product);
            } else {
                ensureProductNotMissing(product);
            }
        }
    }

    @Transactional
    public void planInventoryReplenishment() {
        List<Forecast> forecasts = forecastRepository.findAll();

        for (Forecast forecast : forecasts) {
            Product product = forecast.getProduct();

            int averageDemand = forecast.getAverageDemand() != null
                    ? forecast.getAverageDemand()
                    : 0;

            double seasonalityCoeff = forecast.getSeasonalityCoeff() != null
                    ? forecast.getSeasonalityCoeff()
                    : 1.0;

            int safetyStock = forecast.getSafetyStock() != null
                    ? forecast.getSafetyStock()
                    : 0;

            int stockPosition = forecast.getStockPosition() != null
                    ? forecast.getStockPosition()
                    : 0;

            Double leadTimeValue = orderRepository.averageLeadTimeDays(product.getId());
            int leadTime = leadTimeValue != null
                    ? (int) Math.round(leadTimeValue)
                    : 0;

            int forecastDemand = (int) Math.ceil(averageDemand * seasonalityCoeff * leadTime);

            int targetStockLevel = forecastDemand + safetyStock;

            int recommendedOrderQuantity = targetStockLevel - stockPosition;

            if (recommendedOrderQuantity < 0) {
                recommendedOrderQuantity = 0;
            }

            forecast.setRecommendedQuantity(recommendedOrderQuantity);
            forecastRepository.save(forecast);
        }
    }

    @Transactional
    public void updateForecastData(Integer forecastId,
                                   Integer averageDemand,
                                   Double seasonalityCoeff,
                                   Integer reorderPoint,
                                   Integer safetyStock,
                                   Integer stockPosition,
                                   Integer recommendedQuantity) {
        Forecast forecast = forecastRepository.findById(forecastId)
                .orElseThrow(() -> new RuntimeException("Prognozė nerasta"));

        forecast.setAverageDemand(averageDemand);
        forecast.setSeasonalityCoeff(seasonalityCoeff);
        forecast.setReorderPoint(reorderPoint);
        forecast.setSafetyStock(safetyStock);
        forecast.setStockPosition(stockPosition);
        forecast.setRecommendedQuantity(recommendedQuantity);

        forecastRepository.save(forecast);
    }

    private Forecast getOrCreateForecast(Product product) {
        return forecastRepository.findFirstByProductId(product.getId())
                .orElseGet(() -> {
                    Forecast forecast = new Forecast();
                    forecast.setId(forecastRepository.findMaxId() + 1);
                    forecast.setProduct(product);
                    forecast.setPeriodStart(LocalDate.now().withDayOfMonth(1));
                    forecast.setPeriodEnd(LocalDate.now().plusMonths(2).withDayOfMonth(1).minusDays(1));
                    forecast.setAverageDemand(0);
                    forecast.setSeasonalityCoeff(1.0);
                    forecast.setReorderPoint(0);
                    forecast.setSafetyStock(0);
                    forecast.setStockPosition(0);
                    forecast.setRecommendedQuantity(0);
                    forecast.setCreatedAt(LocalDate.now());
                    return forecastRepository.save(forecast);
                });
    }

    private void createMissingProduct(Product product) {
        if (lowStockItemRepository.findByProductId(product.getId()).isPresent()) {
            return;
        }

        LowStockItem item = new LowStockItem();
        item.setId(lowStockItemRepository.findMaxId() + 1);
        item.setCreatedAt(LocalDate.now());
        item.setProduct(product);

        lowStockItemRepository.save(item);
    }

    private void ensureProductNotMissing(Product product) {
        lowStockItemRepository.findByProductId(product.getId())
                .ifPresent(lowStockItemRepository::delete);
    }
}