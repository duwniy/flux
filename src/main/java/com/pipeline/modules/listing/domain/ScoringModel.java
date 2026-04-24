package com.pipeline.modules.listing.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scoring_model_versions")
public class ScoringModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "version_number", nullable = false, unique = true)
    private Integer versionNumber;

    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "factor_weights", nullable = false, columnDefinition = "jsonb")
    private Map<String, Integer> factorWeights;

    @Column(name = "is_active")
    private Boolean isActive = false;

    private String description;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Map<String, Integer> getFactorWeights() { return factorWeights; }
    public void setFactorWeights(Map<String, Integer> factorWeights) { this.factorWeights = factorWeights; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
