package wms.sandeliukas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.Forecast;
import wms.sandeliukas.model.LowStockItem;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.repositories.ForecastRepository;
import wms.sandeliukas.repositories.LowStockItemRepository;
import wms.sandeliukas.repositories.OrderRepository;
import wms.sandeliukas.repositories.ProductRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    public List<Forecast> requestForecastWindow() {
        analyzeSalesHistory();
        determineSeasonality();
        calculateInventoryBalance();

        CompletableFuture<Void> determineMissingProducts =
                CompletableFuture.runAsync(this::determineMissingProductsByReorderPoint);

        CompletableFuture<Void> planReplenishment =
                CompletableFuture.runAsync(this::planInventoryReplenishment);

        CompletableFuture.allOf(determineMissingProducts, planReplenishment).join();

        return getForecastData();
    }

    public List<Forecast> getForecastData() {
        return forecastRepository.findAll();
    }

    @Transactional
    public void analyzeSalesHistory() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            Integer totalSold = dataRequestOrder(product.getId());

            Integer months = orderByTimeFrame(product.getId());

            Integer aggregatedSales = orderAggregationByTimeFrame(totalSold);

            int averageDemand = averageDemand(aggregatedSales, months);

            forecast.setAverageDemand(averageDemand);
            forecastRepository.save(forecast);
        }
    }

    private Integer dataRequestOrder(Integer productId) {
        return orderRepository.getTotalSold(productId);
    }

    private Integer orderByTimeFrame(Integer productId) {
        return orderRepository.countSalesMonths(productId);
    }

    private Integer orderAggregationByTimeFrame(Integer totalSold) {
        return totalSold != null ? totalSold : 0;
    }

    private int averageDemand(Integer totalSold, Integer months) {
        if (months == null || months <= 0) {
            return 0;
        }

        return (int) Math.round(totalSold / (double) months);
    }

    @Transactional
    public void determineSeasonality() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            Integer totalSold = getSalesData(product.getId());

            Integer monthCount = aggregateSalesByMonth(product.getId());

            if (monthCount == null || monthCount < 6) {
                setSeasonalityCoefficient(forecast, 1.0);
                continue;
            }

            double overallAverage = calculateOverallAverage(totalSold, monthCount);

            double periodAverage = calculatePeriodAverage(product.getId(), forecast);

            double coefficient = calculateSeasonalityCoefficient(overallAverage, periodAverage);

            saveSeasonalityCoefficient(forecast, coefficient);
        }
    }

    private Integer getSalesData(Integer productId) {
        return orderRepository.getTotalSold(productId);
    }

    private Integer aggregateSalesByMonth(Integer productId) {
        return orderRepository.countSalesMonths(productId);
    }

    private double calculateOverallAverage(Integer totalSold, Integer monthCount) {
        if (monthCount == null || monthCount == 0) {
            return 0;
        }

        return totalSold / (double) monthCount;
    }

    private double calculatePeriodAverage(Integer productId, Forecast forecast) {
        Integer periodSold = orderRepository.getPeriodSold(
                productId,
                forecast.getPeriodStart(),
                forecast.getPeriodEnd()
        );

        long periodMonths = ChronoUnit.MONTHS.between(
                forecast.getPeriodStart().withDayOfMonth(1),
                forecast.getPeriodEnd().withDayOfMonth(1)
        ) + 1;

        if (periodMonths <= 0) {
            return 0;
        }

        return periodSold / (double) periodMonths;
    }

    private double calculateSeasonalityCoefficient(double overallAverage, double periodAverage) {
        if (overallAverage == 0) {
            return 1.0;
        }

        double coefficient = periodAverage / overallAverage;
        return Math.round(coefficient * 100.0) / 100.0;
    }

    private void saveSeasonalityCoefficient(Forecast forecast, double coefficient) {
        forecast.setSeasonalityCoeff(coefficient);
        forecastRepository.save(forecast);
    }

    private void setSeasonalityCoefficient(Forecast forecast, double coefficient) {
        forecast.setSeasonalityCoeff(coefficient);
        forecastRepository.save(forecast);
    }

    @Transactional
    public void calculateInventoryBalance() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            Forecast forecast = getOrCreateForecast(product);

            Product productData = getProductData(product.getId());

            Integer ordersData = getOrdersData(product.getId());

            int currentStock = calculateCurrentStock(productData);

            saveCurrentStock(productData, currentStock);

            int stockPosition = calculateStockPosition(currentStock, ordersData);

            saveStockPosition(forecast, stockPosition);
        }
    }

    private Product getProductData(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));
    }

    private Integer getOrdersData(Integer productId) {
        return orderRepository.incomingStock(productId);
    }

    private int calculateCurrentStock(Product product) {
        return product.getCurrentStock() != null ? product.getCurrentStock() : 0;
    }

    private void saveCurrentStock(Product product, int currentStock) {
        product.setCurrentStock(currentStock);
        productRepository.save(product);
    }

    private int calculateStockPosition(int currentStock, Integer incomingStock) {
        int incoming = incomingStock != null ? incomingStock : 0;
        return currentStock + incoming;
    }

    private void saveStockPosition(Forecast forecast, int stockPosition) {
        forecast.setStockPosition(stockPosition);
        forecastRepository.save(forecast);
    }

    @Transactional
    public void determineMissingProductsByReorderPoint() {

        List<Forecast> forecasts = getForecastData();

        for (Forecast forecast : forecasts) {

            Product product = forecast.getProduct();

            int stockPositionData = getStockPositionData(forecast);

            int reorderPoint = calculateReorderPoint(forecast, product);

            saveReorderPoint(forecast, reorderPoint);

            if (reorderPoint > stockPositionData) {

                createMissingProduct(product);

            } else {

                ensureProductNotMissing(product);
            }
        }
    }

    private int getStockPositionData(Forecast forecast) {

        return forecast.getStockPosition() != null
                ? forecast.getStockPosition()
                : 0;
    }

    private int calculateReorderPoint(Forecast forecast, Product product) {
        int averageDemand = forecast.getAverageDemand() != null ? forecast.getAverageDemand() : 0;
        int safetyStock = forecast.getSafetyStock() != null ? forecast.getSafetyStock() : 0;

        Double leadTimeValue = orderRepository.averageLeadTimeDays(product.getId());
        double leadTimeDays = leadTimeValue != null ? leadTimeValue : 0.0;

        return (int) Math.ceil(
                averageDemand * (leadTimeDays / 30.0) + safetyStock
        );
    }

    private void saveReorderPoint(Forecast forecast, int reorderPoint) {
        forecast.setReorderPoint(reorderPoint);
        forecastRepository.save(forecast);
    }

    @Transactional
    public void planInventoryReplenishment() {
        List<Forecast> forecasts = getForecastData();

        for (Forecast forecast : forecasts) {

            int forecastDemand = calculateForecastDemand(forecast);

            int targetStockLevel = calculateTargetStockLevel(forecast, forecastDemand);

            int recommendedOrderQuantity = calculateRecommendedOrderQuantity(forecast, targetStockLevel);

            if (recommendedOrderQuantity > 0) {
                saveRecommendedOrderQuantity(forecast, recommendedOrderQuantity);
            } else {
                saveRecommendedOrderQuantity(forecast, 0);
            }
        }
    }

    private int calculateForecastDemand(Forecast forecast) {
        Product product = forecast.getProduct();

        int averageDemand = forecast.getAverageDemand() != null ? forecast.getAverageDemand() : 0;
        double seasonalityCoeff = forecast.getSeasonalityCoeff() != null ? forecast.getSeasonalityCoeff() : 1.0;

        Double leadTimeValue = orderRepository.averageLeadTimeDays(product.getId());
        double leadTimeDays = leadTimeValue != null ? leadTimeValue : 0.0;

        return (int) Math.ceil(
                averageDemand * seasonalityCoeff * (leadTimeDays / 30.0)
        );
    }

    private int calculateTargetStockLevel(Forecast forecast, int forecastDemand) {
        int safetyStock = forecast.getSafetyStock() != null ? forecast.getSafetyStock() : 0;
        return forecastDemand + safetyStock;
    }

    private int calculateRecommendedOrderQuantity(Forecast forecast, int targetStockLevel) {
        int stockPosition = forecast.getStockPosition() != null ? forecast.getStockPosition() : 0;
        return targetStockLevel - stockPosition;
    }

    private void saveRecommendedOrderQuantity(Forecast forecast, int recommendedOrderQuantity) {
        forecast.setRecommendedQuantity(Math.max(recommendedOrderQuantity, 0));
        forecastRepository.save(forecast);
    }

    @Transactional
    public void submitForecastChanges(Integer forecastId,
                                      Integer averageDemand,
                                      Double seasonalityCoeff,
                                      Integer reorderPoint,
                                      Integer safetyStock,
                                      Integer stockPosition,
                                      Integer recommendedQuantity) {
        updateForecastData(
                forecastId,
                averageDemand,
                seasonalityCoeff,
                reorderPoint,
                safetyStock,
                stockPosition,
                recommendedQuantity
        );
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