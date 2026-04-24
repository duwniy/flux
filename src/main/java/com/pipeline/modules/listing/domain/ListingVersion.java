package com.pipeline.modules.listing.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listing_versions")
public class ListingVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    private String description;

    @Column(name = "photos_count")
    private Integer photosCount;

    private Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown")
    private JsonNode scoreBreakdown;

    @Column(name = "change_reason")
    private String changeReason;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "is_current")
    private Boolean isCurrent = true;

    @Column(name = "scoring_model_id")
    private UUID scoringModelId;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getListingId() { return listingId; }
    public void setListingId(UUID listingId) { this.listingId = listingId; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPhotosCount() { return photosCount; }
    public void setPhotosCount(Integer photosCount) { this.photosCount = photosCount; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public JsonNode getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(JsonNode scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public Instant getValidFrom() { return validFrom; }
    public void setValidFrom(Instant validFrom) { this.validFrom = validFrom; }

    public Instant getValidTo() { return validTo; }
    public void setValidTo(Instant validTo) { this.validTo = validTo; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean current) { isCurrent = current; }

    public UUID getScoringModelId() { return scoringModelId; }
    public void setScoringModelId(UUID scoringModelId) { this.scoringModelId = scoringModelId; }
}
