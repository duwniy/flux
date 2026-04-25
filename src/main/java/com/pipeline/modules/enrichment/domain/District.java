package com.pipeline.modules.enrichment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "districts")
@Getter
@Setter
public class District {

    @Id
    private String id; // district name e.g. "district-1"

    private String name;

    private String city;

    @Column(name = "median_price_sqm")
    private BigDecimal medianPriceSqm;

    @Column(name = "demand_index")
    private BigDecimal demandIndex;

    @Column(name = "active_listings_count")
    private Integer activeListingsCount;

    @Column(name = "avg_days_on_market")
    private BigDecimal avgDaysOnMarket;

    @Column(name = "seasonal_index")
    private BigDecimal seasonalIndex;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
