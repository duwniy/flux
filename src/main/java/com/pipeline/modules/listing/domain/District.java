package com.pipeline.modules.listing.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "districts")
public class District {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(name = "median_price_sqm", precision = 12, scale = 2)
    private BigDecimal medianPriceSqm;

    @Column(name = "demand_index", precision = 4, scale = 2)
    private BigDecimal demandIndex;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "active_listings_count")
    private Integer activeListingsCount;

    @Column(name = "avg_days_on_market", precision = 5, scale = 1)
    private BigDecimal avgDaysOnMarket;

    @Column(name = "seasonal_index", precision = 4, scale = 2)
    private BigDecimal seasonalIndex;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getMedianPriceSqm() { return medianPriceSqm; }
    public void setMedianPriceSqm(BigDecimal medianPriceSqm) { this.medianPriceSqm = medianPriceSqm; }

    public BigDecimal getDemandIndex() { return demandIndex; }
    public void setDistrictDemandIndex(BigDecimal demandIndex) { this.demandIndex = demandIndex; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Integer getActiveListingsCount() { return activeListingsCount; }
    public void setActiveListingsCount(Integer activeListingsCount) { this.activeListingsCount = activeListingsCount; }

    public BigDecimal getAvgDaysOnMarket() { return avgDaysOnMarket; }
    public void setAvgDaysOnMarket(BigDecimal avgDaysOnMarket) { this.avgDaysOnMarket = avgDaysOnMarket; }

    public BigDecimal getSeasonalIndex() { return seasonalIndex; }
    public void setSeasonalIndex(BigDecimal seasonalIndex) { this.seasonalIndex = seasonalIndex; }
}
