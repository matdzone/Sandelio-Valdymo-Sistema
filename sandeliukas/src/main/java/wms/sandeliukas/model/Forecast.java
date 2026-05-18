package wms.sandeliukas.model;

import jakarta.persistence.*;

import java.time.LocalDate;


@Entity
@Table(name = "Forecast")
public class Forecast {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "periodStart")
    private LocalDate periodStart;

    @Column(name = "periodEnd")
    private LocalDate periodEnd;

    @Column(name = "averageDemand")
    private Integer averageDemand;

    @Column(name = "seasonalityCoeff")
    private Double seasonalityCoeff;

    @Column(name = "reorderPoint")
    private Integer reorderPoint;

    @Column(name = "safetyStock")
    private Integer safetyStock;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @Column(name = "stockPosition")
    private Integer stockPosition;

    @Column(name = "recommendedQuantity")
    private Integer recommendedQuantity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_Product")
    private Product product;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public Integer getAverageDemand() { return averageDemand; }
    public void setAverageDemand(Integer averageDemand) { this.averageDemand = averageDemand; }

    public Double getSeasonalityCoeff() { return seasonalityCoeff; }
    public void setSeasonalityCoeff(Double seasonalityCoeff) { this.seasonalityCoeff = seasonalityCoeff; }

    public Integer getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(Integer reorderPoint) { this.reorderPoint = reorderPoint; }

    public Integer getSafetyStock() { return safetyStock; }
    public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public Integer getStockPosition() { return stockPosition; }
    public void setStockPosition(Integer stockPosition) { this.stockPosition = stockPosition; }

    public Integer getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(Integer recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public void updateForecastData(Integer averageDemand,
                                   Double seasonalityCoeff,
                                   Integer reorderPoint,
                                   Integer safetyStock,
                                   Integer stockPosition,
                                   Integer recommendedQuantity) {
        this.averageDemand = averageDemand;
        this.seasonalityCoeff = seasonalityCoeff;
        this.reorderPoint = reorderPoint;
        this.safetyStock = safetyStock;
        this.stockPosition = stockPosition;
        this.recommendedQuantity = recommendedQuantity;
    }

    public void setSeasonalityCoefficient(double coefficient) {
        this.seasonalityCoeff = coefficient;
    }

    public ForecastData getForecastData() {
        return new ForecastData(
                stockPosition != null ? stockPosition : 0,
                averageDemand != null ? averageDemand : 0,
                safetyStock   != null ? safetyStock   : 0,
                reorderPoint  != null ? reorderPoint  : 0,
                product
        );
    }

    public record ForecastData(
            int stockPosition,
            int averageDemand,
            int safetyStock,
            int reorderPoint,
            Product product
    ) {}
}
