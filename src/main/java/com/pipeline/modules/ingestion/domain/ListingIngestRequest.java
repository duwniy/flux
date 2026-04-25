package com.pipeline.modules.ingestion.domain;

import jakarta.validation.constraints.*;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

import com.pipeline.modules.ingestion.domain.annotations.ValidAreaConsistency;
import com.pipeline.modules.ingestion.domain.annotations.ValidFloorConsistency;
import com.pipeline.modules.ingestion.domain.annotations.ValidPriceRange;
import com.pipeline.core.shared.SellerType;

@ValidFloorConsistency
@ValidPriceRange
@ValidAreaConsistency
@Schema(description = "Request object for creating a new listing")
public record ListingIngestRequest(

    @NotBlank(message = "sellerId is required")
    @Schema(example = "seller-123", description = "Unique ID of the seller")
    String sellerId,

    @NotBlank(message = "title is required")
    @Size(min = 10, max = 200)
    @Schema(example = "Beautiful 2-bedroom apartment in city center", description = "Length: 10-200 chars")
    String title,

    @Schema(example = "Spacious apartment with high ceilings...", description = "Full description of the property")
    String description,

    @NotNull
    @Schema(example = "15000000", description = "Price in local currency")
    BigDecimal price,

    @NotNull
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "10000.0")
    @Schema(example = "75.5", description = "Total area in square meters (1-10000)")
    BigDecimal totalAreaSqm,

    @NotBlank
    @Schema(example = "hamovniki", description = "District identifier (e.g., from districts table)")
    String districtId,

    @Min(1) @Max(100)
    @Schema(example = "5", description = "Floor number (1-100). Optional.", nullable = true)
    Integer floor,

    @Min(1) @Max(100)
    @Schema(example = "12", description = "Total floors in building (1-100). Optional.", nullable = true)
    Integer totalFloors,

    @Min(0) @Max(50)
    @Schema(example = "5", description = "Number of photos provided. Defaults to 0.")
    Integer photosCount,

    @NotNull
    @Schema(example = "OWNER", description = "Type of seller")
    SellerType sellerType  // OWNER, AGENCY, DEVELOPER
) {}
