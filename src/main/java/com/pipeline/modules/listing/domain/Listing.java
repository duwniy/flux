package com.pipeline.modules.listing.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.pipeline.core.shared.ListingStatus;
import com.pipeline.core.shared.SellerType;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "total_area_sqm", nullable = false, precision = 8, scale = 2)
    private BigDecimal totalAreaSqm;

    @Column(name = "district_id", nullable = false)
    private String districtId;

    private Integer floor;

    @Column(name = "total_floors")
    private Integer totalFloors;

    @Column(name = "photos_count")
    private Integer photosCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_type", nullable = false)
    private SellerType sellerType;

    @Enumerated(EnumType.STRING)
    private ListingStatus status;

    private Integer score;

    private Integer version = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown")
    private JsonNode scoreBreakdown;

    @Column(name = "scored_at")
    private Instant scoredAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "anomalies", columnDefinition = "jsonb")
    private JsonNode anomalies;

    @Column(name = "district_median_price_sqm", precision = 12, scale = 2)
    private BigDecimal districtMedianPriceSqm;

    @Column(name = "price_deviation_pct", precision = 6, scale = 2)
    private BigDecimal priceDeviationPct;

    @Column(name = "district_demand_index", precision = 4, scale = 2)
    private BigDecimal districtDemandIndex;

    @Column(name = "competitor_count")
    private Integer competitorCount;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    @Column(name = "anomaly_flags")
    private String[] anomalyFlags;

    @Column(name = "enriched_at")
    private Instant enrichedAt;

    @Column(name = "enrichment_status")
    private String enrichmentStatus = "PENDING";

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getTotalAreaSqm() { return totalAreaSqm; }
    public void setTotalAreaSqm(BigDecimal totalAreaSqm) { this.totalAreaSqm = totalAreaSqm; }

    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Integer getTotalFloors() { return totalFloors; }
    public void setTotalFloors(Integer totalFloors) { this.totalFloors = totalFloors; }

    public Integer getPhotosCount() { return photosCount; }
    public void setPhotosCount(Integer photosCount) { this.photosCount = photosCount; }

    public SellerType getSellerType() { return sellerType; }
    public void setSellerType(SellerType sellerType) { this.sellerType = sellerType; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public JsonNode getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(JsonNode scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public Instant getScoredAt() { return scoredAt; }
    public void setScoredAt(Instant scoredAt) { this.scoredAt = scoredAt; }

    public JsonNode getAnomalies() { return anomalies; }
    public void setAnomalies(JsonNode anomalies) { this.anomalies = anomalies; }

    public BigDecimal getDistrictMedianPriceSqm() { return districtMedianPriceSqm; }
    public void setDistrictMedianPriceSqm(BigDecimal districtMedianPriceSqm) { this.districtMedianPriceSqm = districtMedianPriceSqm; }

    public BigDecimal getPriceDeviationPct() { return priceDeviationPct; }
    public void setPriceDeviationPct(BigDecimal priceDeviationPct) { this.priceDeviationPct = priceDeviationPct; }

    public BigDecimal getDistrictDemandIndex() { return districtDemandIndex; }
    public void setDistrictDemandIndex(BigDecimal districtDemandIndex) { this.districtDemandIndex = districtDemandIndex; }

    public Integer getCompetitorCount() { return competitorCount; }
    public void setCompetitorCount(Integer competitorCount) { this.competitorCount = competitorCount; }

    public Boolean getIsAnomaly() { return isAnomaly; }
    public void setIsAnomaly(Boolean isAnomaly) { this.isAnomaly = isAnomaly; }

    public String[] getAnomalyFlags() { return anomalyFlags; }
    public void setAnomalyFlags(String[] anomalyFlags) { this.anomalyFlags = anomalyFlags; }

    public Instant getEnrichedAt() { return enrichedAt; }
    public void setEnrichedAt(Instant enrichedAt) { this.enrichedAt = enrichedAt; }

    public String getEnrichmentStatus() { return enrichmentStatus; }
    public void setEnrichmentStatus(String enrichmentStatus) { this.enrichmentStatus = enrichmentStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) status = ListingStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
